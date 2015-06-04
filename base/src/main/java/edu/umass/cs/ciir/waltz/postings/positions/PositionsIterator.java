package edu.umass.cs.ciir.waltz.postings.positions;

import edu.umass.cs.ciir.waltz.postings.extents.iter.AExtentsIterator;

/**
 * @author jfoley
 */
public class PositionsIterator extends AExtentsIterator {
  private final PositionsList data;

  public PositionsIterator(PositionsList simplePositionsList) {
    super(simplePositionsList.size());
    this.data = simplePositionsList;
  }

  @Override
  public int currentBegin() {
    return data.getPosition(pos);
  }

  @Override
  public int currentEnd() {
    return data.getPosition(pos) + 1;
  }
}
