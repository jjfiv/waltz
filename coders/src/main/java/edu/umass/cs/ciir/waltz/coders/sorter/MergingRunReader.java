package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.collections.iters.ClosingIterator;
import ciir.jfoley.chai.io.FS;
import ciir.jfoley.chai.io.IO;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.reduce.ClosingSinkFn;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author jfoley.
 */
public class MergingRunReader<T> implements Closeable, ClosingIterator<T> {
  private final PriorityQueue<SortingRunReader<T>> queue;

  public MergingRunReader(Collection<SortingRunReader<T>> readers) {
    this.queue = new PriorityQueue<>();
    for (SortingRunReader<T> reader : readers) {
      queue.offer(reader);
    }
  }

  @SuppressWarnings("SimplifiableIfStatement")
  @Override
  public boolean hasNext() {
    if(queue.isEmpty()) return false;
    return queue.peek().hasNext();
  }

  public T peek() {
    if(queue.isEmpty()) return null;
    return queue.peek().peek();
  }

  @Override
  public T next() {
    SortingRunReader<T> minimum = queue.poll();
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
    for (SortingRunReader<T> tRunReader : queue) {
      tRunReader.close();
    }
    queue.clear();
  }

  /**
   * This is actually more efficient than iterating, since it adds and removes from the priority queue as minimally as possible, and assumes there's order within the runs.
   * @param collector callback function that handles each item in turn.
   * @throws IOException
   */
  public void forAll(ClosingSinkFn<T> collector) throws IOException {
    while (queue.size() > 1) {
      // find minimum, pull it out:
      SortingRunReader<T> minimum = queue.poll();
      // go until nextBest needs to go.
      SortingRunReader<T> nextBest = queue.peek();

      while(minimum.hasNext() && minimum.compareTo(nextBest) <= 0) {
        collector.process(minimum.next());
      }
      if(minimum.hasNext()) {
        queue.offer(minimum);
      } else {
        minimum.close();
      }
    }

    if(queue.size() == 1) {
      SortingRunReader<T> last = queue.poll();
      while(last.hasNext()) {
        collector.process(last.next());
      }
      last.close();
    }
  }

  public static <T> MergingRunReader<T> openDirectory(File dir, Comparator<? super T> cmp, Coder<T> itemCoder) throws IOException {
    List<SortingRunReader<T>> readers = new ArrayList<>();
    for (File file : FS.listDirectory(dir)) {
      readers.add(new SortingRunReader<>(cmp, itemCoder, file));
    }
    return new MergingRunReader<>(readers);
  }
}
