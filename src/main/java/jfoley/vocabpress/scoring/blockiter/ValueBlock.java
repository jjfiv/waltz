package jfoley.vocabpress.scoring.blockiter;

import jfoley.vocabpress.scoring.Posting;

import java.util.AbstractList;
import java.util.List;

/**
 * @author jfoley
 */
public class ValueBlock<X extends Posting> extends AbstractList<X> {
  private final List<X> vals;

  public ValueBlock(List<X> vals) {
    this.vals = vals;
  }
  public int min() {
    return vals.get(0).getKey();
  }
  public int max() {
    return vals.get(vals.size()-1).getKey();
  }

  @Override
  public int size() {
    return vals.size();
  }
  @Override
  public X get(int index) {
    return vals.get(index);
  }
}
