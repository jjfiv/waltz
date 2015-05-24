package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.collections.iters.ClosingIterator;
import ciir.jfoley.chai.io.FS;
import ciir.jfoley.chai.io.IO;
import edu.umass.cs.ciir.waltz.coders.Coder;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author jfoley.
 */
public class MergingRunReader<T> implements Closeable, ClosingIterator<T> {
  private final PriorityQueue<RunReader<T>> queue;

  public MergingRunReader(Collection<RunReader<T>> readers) {
    this.queue = new PriorityQueue<>();
    for (RunReader<T> reader : readers) {
      queue.offer(reader);
    }
  }

  @SuppressWarnings("SimplifiableIfStatement")
  @Override
  public boolean hasNext() {
    if(queue.isEmpty()) return false;
    return queue.peek().hasNext();
  }

  @Override
  public T next() {
    RunReader<T> minimum = queue.poll();
    T obj = minimum.next();
    if (minimum.hasNext()) {
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
      readers.add(new RunReader<>(cmp, countCoder, itemCoder, IO.openInputStream(file)));
    }
    return new MergingRunReader<>(readers);
  }
}
