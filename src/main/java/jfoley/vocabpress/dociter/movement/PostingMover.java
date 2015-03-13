package jfoley.vocabpress.dociter.movement;

/**
 * @author jfoley
 */
public interface PostingMover<X> extends Mover {
  /** always call matches(doc id) before calling this to make sure you're reading what you think you're reading! */
  public X getCurrentPosting();
}
