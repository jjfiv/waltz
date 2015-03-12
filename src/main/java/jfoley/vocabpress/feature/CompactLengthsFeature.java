package jfoley.vocabpress.feature;

import ciir.jfoley.chai.collections.list.IntList;
import jfoley.vocabpress.postings.CountPosting;
import jfoley.vocabpress.postings.impl.SimpleCountPosting;

/**
 * @author jfoley
 */
public class CompactLengthsFeature implements Feature<Integer> {
  private final IntList data;

  public CompactLengthsFeature(IntList lengths) {
    this.data = lengths;
  }
  @Override
  public boolean hasFeature(int key) {
    assert(key >= 0);
    return key < data.size();
  }

  @Override
  public Integer getFeature(int key) {
    assert(hasFeature(key));
    return data.getQuick(key);
  }
}
