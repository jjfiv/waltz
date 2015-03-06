package jfoley.vocabpress.scoring.blockiter;

import jfoley.vocabpress.scoring.Posting;

import java.util.AbstractList;
import java.util.List;

/**
 * @author jfoley
 */
public class ValueBlock<X extends Posting> implements IValueBlock {
  private final List<X> vals;

  public ValueBlock(List<X> vals) {
    this.vals = vals;
  }
  @Override
  public int minKey() {
    return vals.get(0).getKey();
  }
  @Override
  public int maxKey() {
    return vals.get(vals.size()-1).getKey();
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
