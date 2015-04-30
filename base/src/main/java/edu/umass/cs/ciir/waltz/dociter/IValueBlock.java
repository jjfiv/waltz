package edu.umass.cs.ciir.waltz.dociter;

/**
 * @author jfoley
 */
public interface IValueBlock<X> {
  int size();
  X getValue(int index);
}
