package jfoley.vocabpress.postings.positions;

import jfoley.vocabpress.postings.extents.ExtentsIterator;

/**
 * @author jfoley
 */
public class PositionsIterator implements ExtentsIterator {
  private final PositionsList data;
  private int pos;
  private final int size;

  public PositionsIterator(PositionsList simplePositionsList) {
    this.data = simplePositionsList;
    this.pos = 0;
    this.size = data.size();
  }

  @Override
  public boolean isDone() {
    return pos >= size;
  }

  @Override
  public boolean next() {
    pos++;
    return !isDone();
  }

  @Override
  public void reset() {
    pos = 0;
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
