package jfoley.vocabpress.scoring.impl;

import jfoley.vocabpress.scoring.Posting;

/**
 * @author jfoley
 */
public abstract class SimplePosting implements Posting {
  protected final int document;

  protected SimplePosting(int document) {
    this.document = document;
  }

  @Override
  public int getKey() {
    return document;
  }

  @Override
  public int compareTo(Posting o) {
    assert(o != null);
    return Integer.compare(document, o.getKey());
  }
}
