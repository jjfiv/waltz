package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.collections.iters.ClosingIterator;
import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.collections.util.MapFns;
import ciir.jfoley.chai.collections.util.QuickSort;
import ciir.jfoley.chai.fn.SinkFn;
import ciir.jfoley.chai.io.FS;
import ciir.jfoley.chai.io.IO;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;

import java.io.*;
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
  final Comparator<T> cmp;
  private final int maxItemsInMemory;
  private final int mergeFactor;
  private final ArrayList<T> buffer;
  private int nextId;
  Map<Integer, List<Integer>> runsByLevel = new HashMap<>();
  private int maxLevelRuns;

  public ExternalSortingWriter(File dir, Coder<T> coder) {
    this(dir, coder, Comparing.defaultComparator());
  }
  public ExternalSortingWriter(File dir, Coder<T> coder, Comparator<T> comparator) {
    // Leverage some defaults from Galago, because why not?
    this(dir, coder, comparator, 50 * 1024 * 1024, 10);
  }
  public ExternalSortingWriter(File dir, Coder<T> coder, Comparator<T> comparator, int maxItemsInMemory, int mergeFactor) {
    assert(dir.isDirectory());
    this.dir = dir;
    this.countCoder = FixedSize.longs;
    this.objCoder = coder;
    this.cmp = comparator;
    this.maxItemsInMemory = maxItemsInMemory;
    this.mergeFactor = mergeFactor;
    this.buffer = new ArrayList<>(maxItemsInMemory);
    this.nextId = 0;
    this.maxLevelRuns = 0;
  }

  @Override
  public void close() throws IOException {
    // push all runs to topmost level:
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
  public void flush() throws IOException {
    int currentId = nextId++;
    try (OutputStream output = IO.openOutputStream(nameForId(currentId).getAbsolutePath())) {
      QuickSort.sort(cmp, buffer);
      // write run to file.
      countCoder.write(output, (long) buffer.size());
      for (T t : buffer) {
        objCoder.write(output, t);
      }
      // ditch memory as soon as possible.
      buffer.clear();
      // add to lowest rung of runs collection.
      MapFns.extendListInMap(runsByLevel, 0, currentId);
    }
    // check and see if we need to mergeRuns()
    mergeRuns();
  }

  public static class SortDirectory<T> implements Iterable<T> {
    private final File dir;
    private final Comparator<T> cmp;
    private final Coder<Long> countCoder;
    private final Coder<T> itemCoder;

    public SortDirectory(File dir, Comparator<T> cmp, Coder<Long> countCoder, Coder<T> itemCoder) throws IOException {
      this.dir = dir;
      this.cmp = cmp;
      this.countCoder = countCoder;
      this.itemCoder = itemCoder;
    }

    @Override
    public MergingRunReader<T> iterator() {
      try {
        return MergingRunReader.openDirectory(dir, cmp, countCoder, itemCoder);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static class MergingRunReader<T> implements Closeable, ClosingIterator<T> {
    private final PriorityQueue<RunReader<T>> queue;

    public MergingRunReader(Collection<RunReader<T>> readers) {
      this.queue = new PriorityQueue<>();
      for (RunReader<T> reader : readers) {
        queue.offer(reader);
      }
    }

    @Override
    public boolean hasNext() {
      return queue.peek().hasNext();
    }

    @Override
    public T next() {
      RunReader<T> minimum = queue.poll();
      T obj = minimum.next();
      if(minimum.hasNext()) {
        queue.offer(minimum);
      } else {
        IO.close(minimum);
      }
      return obj;
    }

    @Override
    public void close() throws IOException {
      for (RunReader<T> tRunReader : queue) {
        tRunReader.close();
      }
      queue.clear();
    }

    public static <T> MergingRunReader<T> openDirectory(File dir, Comparator<T> cmp, Coder<Long> countCoder, Coder<T> itemCoder) throws IOException {
      List<RunReader<T>> readers = new ArrayList<>();
      for (File file : FS.listDirectory(dir)) {
        readers.add(new RunReader<T>(cmp, countCoder, itemCoder, IO.openInputStream(file)));
      }
      return new MergingRunReader<>(readers);
    }
  }

  public static class RunReader<T> implements Closeable, Comparable<RunReader<T>> {
    private final InputStream source;
    private final long count;
    private final Coder<T> itemCoder;
    private final Comparator<T> cmp;
    private T nextItem;
    private int index;

    public RunReader(Comparator<T> cmp, Coder<Long> countCoder, Coder<T> itemCoder, InputStream source) {
      this.cmp = cmp;
      this.itemCoder = itemCoder;
      this.source = source;
      this.count = countCoder.read(source);
      index = 0;
      next();
    }

    public T peek() {
      return nextItem;
    }

    public boolean hasNext() {
      return nextItem != null;
    }

    public T next() {
      T lastItem = nextItem;
      if(index < count) {
        this.nextItem = itemCoder.read(source);
        this.index++;
      } else {
        nextItem = null;
      }
      return lastItem;
    }

    @Override
    public void close() throws IOException {
      source.close();
    }

    @Override
    public int compareTo(RunReader<T> o) {
      return cmp.compare(peek(), o.peek());
    }
  }

  private void mergeRuns() throws IOException {
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

  private Integer mergeRuns(List<Integer> runs) throws IOException {
    int currentId = nextId++;
    PriorityQueue<RunReader<T>> readers = new PriorityQueue<>();
    long total = 0L;
    for (int run : runs) {
      RunReader<T> rdr = new RunReader<>(cmp, countCoder, objCoder, IO.openInputStream(nameForId(run)));
      total += rdr.count;
      readers.add(rdr);
    }

    try (OutputStream writer = IO.openOutputStream(nameForId(currentId).getAbsolutePath())) {
      countCoder.write(writer, total);
      while (readers.size() > 1) {
        // find minimum, pull it out:
        RunReader<T> minimum = readers.poll();
        RunReader<T> nextBest = readers.peek();

        while(minimum.hasNext() && minimum.compareTo(nextBest) <= 0) {
          objCoder.write(writer, minimum.next());
        }
        if(minimum.hasNext()) {
          readers.offer(minimum);
        } else {
          minimum.close();
        }
      }

      if(readers.size() == 1) {
        RunReader<T> last = readers.poll();
        while(last.hasNext()) {
          objCoder.write(writer, last.next());
        }
        last.close();
      }
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
  public void process(T input) {
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
