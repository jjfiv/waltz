package jfoley.vocabpress.scoring.list;

import jfoley.vocabpress.scoring.Posting;

/**
 * A posting list is an interface to a set of postings.
 * @author jfoley
 */
public interface PostingList {
  public Iterable<Posting> getPostings();
}
