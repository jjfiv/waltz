package jfoley.vocabpress.scoring.list;

import jfoley.vocabpress.scoring.iter.PostingIterator;

/**
 * A posting list is an interface to a set of postings.
 * @author jfoley
 */
public interface PostingList {
  public PostingIterator getPostings();
}
