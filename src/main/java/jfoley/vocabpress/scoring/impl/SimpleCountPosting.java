package jfoley.vocabpress.scoring.impl;

import jfoley.vocabpress.scoring.CountPosting;
import jfoley.vocabpress.scoring.Posting;

/**
 * @author jfoley
 */
public class SimpleCountPosting extends SimplePosting implements CountPosting{
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
