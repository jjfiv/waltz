package jfoley.vocabpress.postings.impl;

import jfoley.vocabpress.postings.APosting;
import jfoley.vocabpress.postings.CountPosting;

/**
 * @author jfoley
 */
public class SimpleCountPosting extends APosting implements CountPosting {
  public final int count;

  public SimpleCountPosting(int document, int count) {
    super(document);
    this.count = count;
  }

  @Override
  public int getCount() {
    return count;
  }
}
