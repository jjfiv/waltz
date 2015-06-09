package edu.umass.cs.ciir.waltz.coders.reduce;

/**
 * @author jfoley
 */
public abstract class Reducer<T> {
  public abstract boolean shouldMerge(T lhs, T rhs);
  public abstract T merge(T lhs, T rhs);

  public static class NullReducer<T> extends Reducer<T> {
    @Override public boolean shouldMerge(T lhs, T rhs) { return false; }
    @Override public T merge(T lhs, T rhs) { throw new UnsupportedOperationException(); }
  }
}
