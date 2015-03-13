package jfoley.vocabpress.postings.impl;

import jfoley.vocabpress.postings.APosting;

/**
 * @author jfoley
 */
public class SimpleCountPosting extends APosting<Integer> {
  public final int count;

  public SimpleCountPosting(int document, int count) {
    super(document);
    this.count = count;
  }

  public int getCount() {
    return count;
  }

  @Override
  public Integer getValue() {
    return count;
  }
}
