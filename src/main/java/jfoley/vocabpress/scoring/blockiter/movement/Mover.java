package jfoley.vocabpress.scoring.blockiter.movement;

import jfoley.vocabpress.scoring.Posting;

/**
 * @author jfoley
 */
public interface Mover extends Comparable<Mover> {
  /** We use Integer.MAX_VALUE here so that we can determine the next in AnyOf and AllOf by using min() */
  public static final int DONE_ID = Integer.MAX_VALUE;

  /** The max key it can see in the future; not the maximum in the list. */
  public int maxKey();
  /** The current key it has access to. */
  public int currentKey();

  /** returns true if it is done */
  public boolean isDone();

  /** Equivalent to movePast(currentKey()) */
  public void next();

  /** Move to the given posting-key. */
  public void moveTo(int key);
  /** Move past the given posting-key */
  public void movePast(int key);
}
