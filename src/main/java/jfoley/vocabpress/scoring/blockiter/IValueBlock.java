package jfoley.vocabpress.scoring.blockiter;

import jfoley.vocabpress.postings.Posting;

/**
 * @author jfoley
 */
public interface IValueBlock<X extends Posting> {
  public int minKey();
  public int maxKey();
  public int size();
  public X getValue(int index);
}
