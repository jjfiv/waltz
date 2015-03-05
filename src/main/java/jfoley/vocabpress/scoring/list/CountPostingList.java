package jfoley.vocabpress.scoring.list;

import jfoley.vocabpress.scoring.iter.CountIterator;

/**
 * @author jfoley
 */
public interface CountPostingList extends PostingList {
  CountIterator getCountIterator();
}
