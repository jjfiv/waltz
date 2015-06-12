package edu.umass.cs.ciir.waltz.coders.reduce;

import ciir.jfoley.chai.collections.iters.ClosingIterator;
import ciir.jfoley.chai.io.IO;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author jfoley
 */
public class ReducingIterator<T> implements ClosingIterator<T> {
  private final Iterator<T> inner;
  private final Reducer<T> reducer;
  private T current;
  private T next;

  public ReducingIterator(Reducer<T> reducer, Iterator<T> inner) {
    this.reducer = reducer;
    this.inner = inner;
    current = pullNext();
    next = null;
    updateCurrentAndNext();
  }

  T pullNext() {
    return inner.hasNext() ? inner.next() : null;
  }

  void updateCurrentAndNext() {
    while(true) {
      next = pullNext();
      if(next == null) break;
      if(reducer.shouldMerge(current, next)) {
        current = reducer.merge(current, next);
      } else break;
    }
  }

  @Override
  public void close() throws Exception {
    IO.close(inner);
  }

  @Override
  public boolean hasNext() {
    return current != null;
  }

  @Override
  public T next() {
    if(!hasNext()) throw new NoSuchElementException();
    T out = current;
    current = next;
    updateCurrentAndNext();
    return out;
  }

  public static <T> Iterable<T> of(Iterable<T> input, Reducer<T> reducer) {
    return () -> new ReducingIterator<>(reducer, input.iterator());
  }
}
