package jfoley.vocabpress.scoring.iter;

import jfoley.vocabpress.scoring.PositionsPosting;

/**
 * @author jfoley
 */
public interface PositionsIterator extends CountIterator {
  public PositionsPosting getPositions(int id);
}
