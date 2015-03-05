package jfoley.vocabpress.scoring.list;

import jfoley.vocabpress.scoring.CountPosting;

/**
 * @author jfoley
 */
public interface CountPostingList extends PostingList {
  Iterable<CountPosting> getCountPostings();
}
