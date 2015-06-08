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

  public T peek() {
    if(queue.isEmpty()) return null;
    return queue.peek().peek();
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

  /**
   * This is actually more efficient than iterating, since it adds and removes from the priority queue as minimally as possible, and assumes there's order within the runs.
   * @param collector callback function that handles each item in turn.
   * @throws IOException
   */
  public void forAll(ClosingSinkFn<T> collector) throws IOException {
    while (queue.size() > 1) {
      // find minimum, pull it out:
      RunReader<T> minimum = queue.poll();
      // go until nextBest needs to go.
      RunReader<T> nextBest = queue.peek();

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
      RunReader<T> last = queue.poll();
      while(last.hasNext()) {
        collector.process(last.next());
      }
      last.close();
    }
    collector.close();
  }

  public static <T> MergingRunReader<T> openDirectory(File dir, Comparator<? super T> cmp, Coder<Long> countCoder, Coder<T> itemCoder) throws IOException {
    List<RunReader<T>> readers = new ArrayList<>();
    for (File file : FS.listDirectory(dir)) {
      readers.add(new RunReader<>(cmp, countCoder, itemCoder, IO.openInputStream(file)));
    }
    return new MergingRunReader<>(readers);
  }
}
