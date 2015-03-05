package jfoley.vocabpress.scoring.impl;

import jfoley.vocabpress.scoring.CountPosting;

/**
 * @author jfoley
 */
public class SimpleCountPosting implements CountPosting {
  public final int document;
  public final int count;

  public SimpleCountPosting(int document, int count) {
    this.document = document;
    this.count = count;
  }

  @Override
  public int getCount() {
    return count;
  }

  @Override
  public int getKey() {
    return document;
  }
}
