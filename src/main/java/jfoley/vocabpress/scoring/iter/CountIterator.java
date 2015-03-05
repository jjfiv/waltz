package jfoley.vocabpress.scoring.iter;

import jfoley.vocabpress.scoring.CountPosting;

/**
 * @author jfoley
 */
public interface CountIterator extends PostingIterator {
  public CountPosting getCount(int id);
}
