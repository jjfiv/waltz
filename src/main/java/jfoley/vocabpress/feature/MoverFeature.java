package jfoley.vocabpress.feature;

import jfoley.vocabpress.dociter.movement.PostingMover;
import jfoley.vocabpress.postings.Posting;

/**
 * @author jfoley
 */
public class MoverFeature<X extends Posting> implements Feature<X> {
  private final PostingMover<X> mover;

  public MoverFeature(PostingMover<X> mover) {
    this.mover = mover;

  }

  @Override
  public boolean hasFeature(int key) {
    mover.moveToAbsolute(key);
    return mover.matches(key);
  }

  @Override
  public X getFeature(int key) {
    mover.moveToAbsolute(key);
    if(mover.matches(key)) {
      return mover.getCurrentPosting();
    }
    return null;
  }
}
