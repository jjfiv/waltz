package jfoley.vocabpress.postings;

import jfoley.vocabpress.dociter.BlockPostingsIterator;

/**
 * @author jfoley
 */
public interface PostingList<X extends Posting> {
  public BlockPostingsIterator<X> getIterator();
}
