package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.collections.iters.ClosingIterator;

import java.io.IOException;

/**
 * @author jfoley
 */
public interface SortedReader<T> extends ClosingIterator<T>, Comparable<SortedReader<T>> {
  T peek();

  boolean hasNext();

  T next();

  @Override
  void close() throws IOException;
}
