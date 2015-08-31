package edu.umass.cs.ciir.waltz.io.postings;

import ciir.jfoley.chai.collections.list.AChaiList;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsIterator;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;

/**
 * @author jfoley
 */
public final class ArrayPosList extends AChaiList<Integer> implements PositionsList {
  final int[] data;
  final int size;

  public ArrayPosList(int[] data, int size) {
    this.data = data;
    this.size = size;
  }

  @Override
  public Integer get(int index) {
    return data[index];
  }

  @Override
  public int getPosition(int index) {
    return data[index];
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public PositionsIterator getSpanIterator() {
    return new PositionsIterator(this);
  }
}
