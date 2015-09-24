package edu.umass.cs.ciir.waltz.postings.positions;

import edu.umass.cs.ciir.waltz.postings.extents.SpanIterable;

import java.util.List;

/**
 * @author jfoley
 */
public interface PositionsList extends SpanIterable, List<Integer> {
  int getPosition(int index);
  int size();
  @Override
  PositionsIterator getSpanIterator();

  default Integer get(int index) {
    return getPosition(index);
  }
}
