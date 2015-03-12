package jfoley.vocabpress.positions;

import jfoley.vocabpress.extents.ExtentIterable;

import java.util.List;

/**
 * @author jfoley
 */
public interface PositionsList extends ExtentIterable {
  public int getPosition(int index);
  public int size();
  @Override
  public PositionsIterator getExtentsIterator();
  public List<Integer> toList();
}
