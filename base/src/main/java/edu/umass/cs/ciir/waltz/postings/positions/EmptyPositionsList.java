package edu.umass.cs.ciir.waltz.postings.positions;

import ciir.jfoley.chai.collections.list.AChaiList;

import java.util.Collections;
import java.util.List;

/**
 * @author jfoley
 */
public class EmptyPositionsList extends AChaiList<Integer> implements PositionsList {
  public static final PositionsList instance = new EmptyPositionsList();

  /** Private because you should use the singleton instance instead. */
  private EmptyPositionsList() {

  }

  @Override
  public Integer get(int index) {
    throw new IndexOutOfBoundsException();
  }

  @Override
  public int getPosition(int index) {
    throw new IndexOutOfBoundsException();
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public PositionsIterator getSpanIterator() {
    return new PositionsIterator(this);
  }
}
