package edu.umass.cs.ciir.waltz.dociter;

/**
 * @author jfoley
 */
public interface IValueBlock<X> {
  public int size();
  public X getValue(int index);
}
