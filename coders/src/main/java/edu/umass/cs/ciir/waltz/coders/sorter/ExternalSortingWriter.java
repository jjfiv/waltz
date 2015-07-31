package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.collections.util.Comparing;
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
public class ExternalSortingWriter<T> extends GeometricItemMerger implements Flushable, Closeable, SinkFn<T> {
  public static final int DEFAULT_MAX_ITEMS_IN_MEMORY = 64*1024;
  private final File dir;
  final Coder<Long> countCoder;
  final Coder<T> objCoder;
  private Reducer<T> reducer = new Reducer.NullReducer<>();
  final Comparator<? super T> cmp;
  private final int maxItemsInMemory;
  private ArrayList<T> buffer;
  private long startTime = System.currentTimeMillis();
  private long endTime = 0;

  public ExternalSortingWriter(File dir, Coder<T> coder) {
    this(dir, coder, Comparing.defaultComparator());
  }
  public ExternalSortingWriter(File dir, Coder<T> coder, Comparator<? super T> comparator) {
    // Leverage some defaults from Galago, because why not?
    this(dir, coder, new Reducer.NullReducer<T>(), comparator, DEFAULT_MAX_ITEMS_IN_MEMORY, DEFAULT_MERGE_FACTOR);
  }
  public ExternalSortingWriter(File dir, Coder<T> coder, Reducer<T> reducer, Comparator<? super T> comparator, int maxItemsInMemory, int mergeFactor) {
    super(mergeFactor);
    this.mergeFn = this::mergeRuns;
    assert(dir.isDirectory());
    this.dir = dir;
    this.countCoder = FixedSize.longs;
    this.objCoder = coder.lengthSafe();
    this.reducer = reducer == null ? new Reducer.NullReducer<>() : reducer;
    this.cmp = comparator;
    this.maxItemsInMemory = maxItemsInMemory;
    this.buffer = new ArrayList<>();

    MemoryNotifier.register(this);
  }

  public ExternalSortingWriter(File dir, Reducer<T> reducer, Coder<T> coder) {
    this(dir, coder, reducer, Comparing.defaultComparator(), DEFAULT_MAX_ITEMS_IN_MEMORY, DEFAULT_MERGE_FACTOR);
  }

  @Override
  public void close() throws IOException {
    // push all runs to topmost level:
    MemoryNotifier.unregister(this);
    flush();
    stop();
    endTime = System.currentTimeMillis();
  }

  public void flushSync() throws IOException {
    flush();
    waitForCurrentJobs();
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

    List<T> items;
    items = buffer;
    buffer = new ArrayList<>();
    int currentId = allocate();

    doAsync(() -> {
      try (ClosingSinkFn<T> writer = getNewWriter(currentId)) {
        QuickSort.sort(cmp, items);
        // write run to file.
        for (T t : items) {
          writer.process(t);
          IO.close(t);
        }

        // ditch memory as soon as possible.
        items.clear();
      } catch (Throwable e) {
        e.printStackTrace(System.err);
        throw new RuntimeException(e);
      }
      // add to lowest rung of runs collection.
      addNewRun(currentId, 0);
    });
    // check and see if we need to mergeRuns()
    checkIfWeCanMergeRuns();
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

  public void mergeRuns(List<Integer> runs, int currentId) throws IOException {
    List<SortedReader<T>> readers = new ArrayList<>();
    for (int run : runs) {
      SortingRunReader<T> rdr = new SortingRunReader<>(cmp, objCoder, nameForId(run));
      if(rdr.peek() == null) {
        throw new AssertionError("fail+"+run+"+ "+rdr.getCount());
      }
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
