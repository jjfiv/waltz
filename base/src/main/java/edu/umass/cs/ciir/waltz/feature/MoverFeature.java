package edu.umass.cs.ciir.waltz.feature;

import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @author jfoley
 */
public class MoverFeature<X> implements Feature<X> {
  private final PostingMover<X> mover;

  public MoverFeature(PostingMover<X> mover) {
    this.mover = Objects.requireNonNull(mover);
  }

  @Override
  public boolean hasFeature(int key) {
    mover.moveToAbsolute(key);
    return mover.matches(key);
  }

  @Override
  @Nullable
  public X getFeature(int key) {
    mover.moveToAbsolute(key);
    if(mover.matches(key)) {
      return mover.getCurrentPosting();
    }
    return null;
  }
}
