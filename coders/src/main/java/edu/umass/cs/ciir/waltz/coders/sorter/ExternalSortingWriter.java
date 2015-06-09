package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.collections.util.MapFns;
import ciir.jfoley.chai.collections.util.QuickSort;
import ciir.jfoley.chai.fn.SinkFn;
import ciir.jfoley.chai.jvm.MemoryNotifier;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.*;

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
  public static final int DEFAULT_MAX_ITEMS_IN_MEMORY = 50*1024*1024;
  public static final int DEFAULT_MERGE_FACTOR = 10;
  private final File dir;
  final Coder<Long> countCoder;
  final Coder<T> objCoder;
  private Reducer<T> reducer = new Reducer.NullReducer<>();
  final Comparator<? super T> cmp;
  private final int maxItemsInMemory;
  private final int mergeFactor;
  private final ArrayList<T> buffer;
  private int nextId;
  Map<Integer, List<Integer>> runsByLevel = new HashMap<>();
  private int maxLevelRuns;

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
    this.buffer = new ArrayList<>(maxItemsInMemory);
    this.nextId = 0;
    this.maxLevelRuns = 0;

    MemoryNotifier.register(this);
  }

  public ExternalSortingWriter(File dir, Reducer<T> reducer, Coder<T> coder) {
    this(dir, coder, reducer, Comparing.defaultComparator(), DEFAULT_MAX_ITEMS_IN_MEMORY, DEFAULT_MERGE_FACTOR);
  }

  @Override
  public synchronized void close() throws IOException {
    // push all runs to topmost level:
    MemoryNotifier.unregister(this);
    flush();
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

    int currentId = nextId++;
    try (ClosingSinkFn<T> writer = getNewWriter(currentId)) {
      QuickSort.sort(cmp, buffer);
      // write run to file.
      buffer.forEach(writer::process);
    }
    // ditch memory as soon as possible.
    buffer.clear();
    // add to lowest rung of runs collection.
    MapFns.extendListInMap(runsByLevel, 0, currentId);
    // check and see if we need to mergeRuns()
    mergeRuns();
  }

  private synchronized void mergeRuns() throws IOException {
    while(true) {
      boolean changed = false;

      for (Map.Entry<Integer, List<Integer>> kv : runsByLevel.entrySet()) {
        int level = kv.getKey();
        List<Integer> runs = kv.getValue();
        if (runs.size() >= mergeFactor) {
          // clear current runs:
          MapFns.extendListInMap(runsByLevel, level+1, mergeRuns(runs));
          maxLevelRuns = Math.max(level+1, maxLevelRuns);
          runsByLevel.remove(level);

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
    List<RunReader<T>> readers = new ArrayList<>();
    for (int run : runs) {
      RunReader<T> rdr = new RunReader<>(cmp, objCoder, nameForId(run));
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
    buffer.add(input);
    if(buffer.size() >= maxItemsInMemory) {
      try {
        flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
