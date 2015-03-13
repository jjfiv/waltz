package jfoley.vocabpress.postings;

import jfoley.vocabpress.dociter.BlockPostingsIterator;

/**
 * @author jfoley
 */
public interface PostingList<V> {
  public BlockPostingsIterator<V> getIterator();
}
