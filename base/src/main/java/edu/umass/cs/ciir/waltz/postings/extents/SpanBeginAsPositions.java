package edu.umass.cs.ciir.waltz.postings.extents;

import ciir.jfoley.chai.collections.list.AChaiList;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsIterator;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;

/**
* @author jfoley
*/
public class SpanBeginAsPositions extends AChaiList<Integer> implements PositionsList {
  private final SpanList extents;

  public SpanBeginAsPositions(SpanList extents) {
    this.extents = extents;
  }

  @Override
  public int size() {
    return extents.size();
  }

  @Override
  public PositionsIterator getSpanIterator() {
    return new PositionsIterator(this);
  }

  @Override
  public int getPosition(int index) {
    return extents.getBegin(index);
  }

  @Override
  public Integer get(int index) {
    return getPosition(index);
  }
}
