package jfoley.vocabpress.dociter.movement;

import jfoley.vocabpress.postings.Posting;

/**
 * @author jfoley
 */
public class EmptyMover<X extends Posting> extends AMover implements PostingMover<X> {
  public static <Y extends Posting> EmptyMover<Y> getInstance() {
    return new EmptyMover<>();
  }

  @Override public void nextBlock() { }
  @Override public void reset() {  }
  @Override public X getCurrentPosting() { return null; }
}
