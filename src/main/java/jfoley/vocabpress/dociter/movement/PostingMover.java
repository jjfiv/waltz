package jfoley.vocabpress.dociter.movement;

import jfoley.vocabpress.postings.Posting;

/**
 * @author jfoley
 */
public interface PostingMover<X extends Posting> extends Mover {
  /** always call matches(doc id) before calling this to make sure you're reading what you think you're reading! */
  public X getCurrentPosting();
}
