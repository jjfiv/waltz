package edu.umass.cs.ciir.waltz.coders.reduce;

/**
 * @author jfoley
 */
public interface MergeFn<T> {
  T merge(T lhs, T rhs);
}
