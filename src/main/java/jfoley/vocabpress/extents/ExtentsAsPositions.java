package jfoley.vocabpress.extents;

import ciir.jfoley.chai.collections.list.IntList;
import jfoley.vocabpress.positions.PositionsIterator;
import jfoley.vocabpress.positions.PositionsList;

import java.util.List;

/**
* @author jfoley
*/
public class ExtentsAsPositions implements PositionsList {
  private final ExtentsList extents;

  public ExtentsAsPositions(ExtentsList extents) {
    this.extents = extents;
  }

  @Override
  public int size() {
    return extents.size();
  }

  @Override
  public PositionsIterator getExtentsIterator() {
    return new PositionsIterator(this);
  }

  @Override
  public List<Integer> toList() {
    IntList data = new IntList();
    for (int i = 0; i < size(); i++) {
      data.add(getPosition(i));
    }
    return data;
  }

  @Override
  public int getPosition(int index) {
    return extents.getBegin(index);
  }
}
