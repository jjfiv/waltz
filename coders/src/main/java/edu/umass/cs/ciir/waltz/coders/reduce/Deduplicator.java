package edu.umass.cs.ciir.waltz.coders.reduce;

import java.util.Objects;

/**
 * @author jfoley
 */
public class Deduplicator<T> extends Reducer<T> {
  @Override
  public boolean shouldMerge(T lhs, T rhs) {
    return Objects.equals(lhs, rhs);
  }

  @Override
  public T merge(T lhs, T rhs) {
    return lhs;
  }
}
