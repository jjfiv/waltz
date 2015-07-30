package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.collections.util.MapFns;
import ciir.jfoley.chai.collections.util.QuickSort;
import ciir.jfoley.chai.fn.SinkFn;
import ciir.jfoley.chai.io.IO;
import ciir.jfoley.chai.jvm.MemoryNotifier;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.files.RunWriter;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.reduce.ClosingSinkFn;
import edu.umass.cs.ciir.waltz.coders.reduce.Reducer;
import edu.umass.cs.ciir.waltz.coders.reduce.SinkReducer;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Break External Sorting into two steps:
 *
 * The writing writes sorted runs to files within a directory.
 * The reading stage combines these while reading in order.
 *
 * <ol>
 *   <li>{link: ExternalSortingWriter} Writing (this class)</li>
 *   <li>{link: ExternalSortingReader} Reading</li>
 * </ol>
 * @author jfoley.
 */
public class ExternalSortingWriter<T> implements Flushable, Closeable, SinkFn<T> {
  public static final int DEFAULT_MAX_ITEMS_IN_MEMORY = 64*1024;
  public static final int DEFAULT_MERGE_FACTOR = 10;
  private final File dir;
  final Coder<Long> countCoder;
  final Coder<T> objCoder;
  private Reducer<T> reducer = new Reducer.NullReducer<>();
  final Comparator<? super T> cmp;
  private final int maxItemsInMemory;
  private final int mergeFactor;
  private ArrayList<T> buffer;
  private int nextId;
  final Map<Integer, List<Integer>> runsByLevel = new ConcurrentHashMap<>();
  private long startTime = System.currentTimeMillis();
  private long endTime = 0;
  public static final ExecutorService asyncExec = ForkJoinPool.commonPool();
  AtomicInteger liveJobs = new AtomicInteger(0);

  public ExternalSortingWriter(File dir, Coder<T> coder) {
    this(dir, coder, Comparing.defaultComparator());
  }
  public ExternalSortingWriter(File dir, Coder<T> coder, Comparator<? super T> comparator) {
    // Leverage some defaults from Galago, because why not?
    this(dir, coder, new Reducer.NullReducer<T>(), comparator, DEFAULT_MAX_ITEMS_IN_MEMORY, DEFAULT_MERGE_FACTOR);
  }
  public ExternalSortingWriter(File dir, Coder<T> coder, Reducer<T> reducer, Comparator<? super T> comparator, int maxItemsInMemory, int mergeFactor) {
    assert(dir.isDirectory());
    this.dir = dir;
    this.countCoder = FixedSize.longs;
    this.objCoder = coder.lengthSafe();
    this.reducer = reducer == null ? new Reducer.NullReducer<>() : reducer;
    this.cmp = comparator;
    this.maxItemsInMemory = maxItemsInMemory;
    this.mergeFactor = mergeFactor;
    this.buffer = new ArrayList<>();
    this.nextId = 0;

    MemoryNotifier.register(this);
  }

  public ExternalSortingWriter(File dir, Reducer<T> reducer, Coder<T> coder) {
    this(dir, coder, reducer, Comparing.defaultComparator(), DEFAULT_MAX_ITEMS_IN_MEMORY, DEFAULT_MERGE_FACTOR);
  }

  public synchronized void doAsync(Runnable r) {
    liveJobs.incrementAndGet();
    asyncExec.submit(() -> {
      r.run();
      liveJobs.decrementAndGet();
    });
  }

  @Override
  public void close() throws IOException {
    // push all runs to topmost level:
    MemoryNotifier.unregister(this);
    flush();
    // merge as many runs as possible.
    do {
      mergeRuns();
      try {
        Thread.sleep(100);
      } catch (InterruptedException ignored) { }
    } while(liveJobs.get() > 0);
    endTime = System.currentTimeMillis();
  }

  public SortDirectory<T> getOutput() throws IOException {
    return new SortDirectory<>(dir, cmp, reducer, objCoder);
  }

  /**
   * Push items out of memory buffer and onto disk.
   * @throws IOException
   */
  @Override
  public synchronized void flush() throws IOException {
    if(buffer.isEmpty()) return;

    List<T> flushingBuffer;
    flushingBuffer = buffer;
    buffer = new ArrayList<>();
    int currentId = nextId++;

    doAsync(new AsyncRunWriter(flushingBuffer, currentId));
    // check and see if we need to mergeRuns()
    mergeRuns();
  }

  public synchronized void addNewRun(int runId, int level)  {
    MapFns.extendListInMap(runsByLevel, level, runId);
  }

  public class AsyncRunWriter implements Runnable {
    private final List<T> items;
    private final int currentId;

    public AsyncRunWriter(List<T> items, int currentId) {
      this.items = items;
      this.currentId = currentId;
    }

    @Override
    public void run() {
      try (ClosingSinkFn<T> writer = getNewWriter(currentId)) {
        QuickSort.sort(cmp, items);
        // write run to file.
        for (T t : items) {
          writer.process(t);
          IO.close(t);
        }

        // ditch memory as soon as possible.
        items.clear();
        // add to lowest rung of runs collection.
        addNewRun(currentId, 0);
      } catch (Throwable e) {
        e.printStackTrace(System.err);
        throw new RuntimeException(e);
      }
    }
  }

  public class AsyncRunMerger implements Runnable {
    private final List<Integer> runs;
    private final int outputLevel;

    public AsyncRunMerger(List<Integer> runs, int outputLevel) {
      this.runs = runs;
      this.outputLevel = outputLevel;
    }

    @Override
    public void run() {
      try {
        addNewRun(mergeRuns(runs), outputLevel);
      } catch (Throwable e) {
        e.printStackTrace(System.err);
        throw new RuntimeException(e);
      }
    }
  }

  private synchronized void mergeRuns() throws IOException {
    while(true) {
      boolean changed = false;

      for (Map.Entry<Integer, List<Integer>> kv : runsByLevel.entrySet()) {
        int level = kv.getKey();
        List<Integer> runs = kv.getValue();
        if (runs.size() >= mergeFactor) {
          runsByLevel.remove(level);

          doAsync(new AsyncRunMerger(runs, level + 1));
          changed = true;
          break;
        }
      }
      if(!changed) break;
    }
  }

  public File nameForId(int id) {
    return new File(dir, Integer.toString(id)+".sorted");
  }

  @Nonnull
  public synchronized ClosingSinkFn<T> getNewWriter(int currentId) throws IOException {
    RunWriter<T> writer = new RunWriter<>(objCoder, nameForId(currentId));
    if(reducer instanceof Reducer.NullReducer) {
      return writer;
    }
    return new SinkReducer<>(reducer, writer);
  }

  private synchronized int mergeRuns(List<Integer> runs) throws IOException {
    int currentId = nextId++;
    List<SortedReader<T>> readers = new ArrayList<>();
    for (int run : runs) {
      SortingRunReader<T> rdr = new SortingRunReader<>(cmp, objCoder, nameForId(run));
      readers.add(rdr);
    }

    try (MergingRunReader<T> reader = new MergingRunReader<>(readers);
         ClosingSinkFn<T> writer = getNewWriter(currentId)) {
        reader.forAll(writer);
    }

    // Delete the files associated with the old runs:
    for (int run : runs) {
      if(!nameForId(run).delete()) {
        throw new IOException("Couldn't delete temporary sort file id="+run+" path="+nameForId(run).getAbsolutePath());
      }
    }

    return currentId;
  }

  @Override
  public synchronized void process(T input) {
    if(input == null) throw new NullPointerException();
    buffer.add(input);
    if(buffer.size() >= maxItemsInMemory) {
      try {
        flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public long getTime() {
    return endTime - startTime;
  }
}
