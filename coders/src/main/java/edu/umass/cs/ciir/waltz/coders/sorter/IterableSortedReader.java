package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.io.IO;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

/**
 * @author jfoley
 */
public class IterableSortedReader<T> implements SortedReader<T> {
  private final Iterator<T> iter;
  private final Comparator<? super T> cmp;
  private T current;

  public IterableSortedReader(Iterable<T> input, Comparator<? super T> cmp) {
    this.cmp = cmp;
    this.iter = input.iterator();
    if(iter.hasNext()) {
      this.current = iter.next();
    } else {
      this.current = null;
    }
  }


  @Override
  public T peek() {
    return current;
  }

  @Override
  public boolean hasNext() {
    return current != null;
  }

  @Override
  public T next() {
    T prev = current;
    current = iter.hasNext() ? iter.next() : null;
    return prev;
  }

  @Override
  public void close() throws IOException {
    IO.close(iter);
  }

  @Override
  public int compareTo(SortedReader<T> o) {
    return cmp.compare(peek(), o.peek());
  }
}
