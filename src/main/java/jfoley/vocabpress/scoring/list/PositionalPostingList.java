package jfoley.vocabpress.scoring.list;

import jfoley.vocabpress.scoring.iter.PositionsIterator;

/**
 * @author jfoley
 */
public interface PositionalPostingList extends CountPostingList {
  public PositionsIterator getPositionalPostings();
}
