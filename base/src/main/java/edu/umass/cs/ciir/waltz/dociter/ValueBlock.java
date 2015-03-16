package edu.umass.cs.ciir.waltz.dociter;

import java.util.List;

/**
 * @author jfoley
 */
public class ValueBlock<X> implements IValueBlock<X> {
  private final List<X> vals;

  public ValueBlock(List<X> vals) {
    this.vals = vals;
  }

  @Override
  public int size() {
    return vals.size();
  }
  @Override
  public X getValue(int index) {
    return vals.get(index);
  }
}
