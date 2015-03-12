package jfoley.vocabpress.dociter;

import jfoley.vocabpress.postings.Posting;

import java.util.List;

/**
 * @author jfoley
 */
public class ValueBlock<X extends Posting> implements IValueBlock<X> {
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
