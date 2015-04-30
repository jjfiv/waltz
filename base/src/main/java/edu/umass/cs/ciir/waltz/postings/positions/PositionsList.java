package edu.umass.cs.ciir.waltz.postings.positions;

import edu.umass.cs.ciir.waltz.postings.extents.ExtentIterable;

import java.util.List;

/**
 * @author jfoley
 */
public interface PositionsList extends ExtentIterable {
  int getPosition(int index);
  int size();
  @Override
  PositionsIterator getExtentsIterator();
  List<Integer> toList();
}
