package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.collections.util.IterableFns;
import ciir.jfoley.chai.collections.util.MapFns;
import ciir.jfoley.chai.collections.util.QuickSort;
import ciir.jfoley.chai.fn.SinkFn;
import ciir.jfoley.chai.io.IO;
import ciir.jfoley.chai.jvm.MemoryNotifier;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;

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
  private final File dir;
  final Coder<Long> countCoder;
  final Coder<T> objCoder;
  final Comparator<? super T> cmp;
  private final int maxItemsInMemory;
  private final int mergeFactor;
  private final ArrayList<T> buffer;
  private int nextId;
  Map<Integer, List<Integer>> runsByLevel = new HashMap<>();
  private int maxLevelRuns;
  private Reducer<T> reducer = new Reducer.NullReducer<>();
  private T previous;

  public ExternalSortingWriter(File dir, Coder<T> coder) {
    this(dir, coder, Comparing.defaultComparator());
  }
  public ExternalSortingWriter(File dir, Coder<T> coder, Comparator<? super T> comparator) {
    // Leverage some defaults from Galago, because why not?
    this(dir, coder, comparator, 50 * 1024 * 1024, 10);
  }
  public ExternalSortingWriter(File dir, Coder<T> coder, Comparator<? super T> comparator, int maxItemsInMemory, int mergeFactor) {
    assert(dir.isDirectory());
    this.dir = dir;
    this.countCoder = FixedSize.longs;
    this.objCoder = coder.lengthSafe();
    this.cmp = comparator;
    this.maxItemsInMemory = maxItemsInMemory;
    this.mergeFactor = mergeFactor;
    this.buffer = new ArrayList<>(maxItemsInMemory);
    this.nextId = 0;
    this.maxLevelRuns = 0;
    this.previous = null;

    MemoryNotifier.register(this);
  }

  @Override
  public synchronized void close() throws IOException {
    // push all runs to topmost level:
    MemoryNotifier.unregister(this);
    flush();
  }

  public SortDirectory<T> getOutput() throws IOException {
    return new SortDirectory<>(dir, cmp, countCoder, objCoder);
  }

  /**
   * Push items out of memory buffer and onto disk.
   * @throws IOException
   */
  @Override
  public synchronized void flush() throws IOException {
    if(previous != null) {
      buffer.add(previous);
      previous = null;
    }
    if(buffer.isEmpty()) return;

    int currentId = nextId++;
    try (RunWriter<T> writer = new RunWriter<>(buffer.size(), countCoder, objCoder, nameForId(currentId))) {
      QuickSort.sort(cmp, buffer);
      // write run to file.
      IterableFns.intoSink(buffer, writer);
      // ditch memory as soon as possible.
      buffer.clear();
      // add to lowest rung of runs collection.
      MapFns.extendListInMap(runsByLevel, 0, currentId);
    }
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
    return new File(dir, Integer.toString(id)+".sorted.gz");
  }

  private synchronized Integer mergeRuns(List<Integer> runs) throws IOException {
    int currentId = nextId++;
    List<RunReader<T>> readers = new ArrayList<>();
    long total = 0L;
    for (int run : runs) {
      RunReader<T> rdr = new RunReader<>(cmp, countCoder, objCoder, IO.openInputStream(nameForId(run)));
      total += rdr.getCount();
      readers.add(rdr);
    }

    try (MergingRunReader<T> reader = new MergingRunReader<>(readers);
         RunWriter<T> writer = new RunWriter<>(total, countCoder, objCoder, nameForId(currentId))) {
      //if(reducer instanceof Reducer.NullReducer) {
        //reader.forAll(writer);
      //} else {
        reader.forAll(new SinkReducer<>(reducer, writer));
      //}
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
    if(previous == null) {
      previous = input;
    } else {
      if(reducer.shouldMerge(previous, input)) {
        previous = reducer.merge(previous, input);
      } else {
        buffer.add(previous);
        previous = input;
      }
    }
    if(buffer.size() >= maxItemsInMemory) {
      try {
        flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
