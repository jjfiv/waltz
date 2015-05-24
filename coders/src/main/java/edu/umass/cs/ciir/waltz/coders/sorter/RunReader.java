package edu.umass.cs.ciir.waltz.coders.sorter;

import edu.umass.cs.ciir.waltz.coders.Coder;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;

/**
 * @author jfoley.
 */
public class RunReader<T> implements Closeable, Comparable<RunReader<T>> {
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
    if (index < count) {
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


  public long getCount() {
    return count;
  }
}
