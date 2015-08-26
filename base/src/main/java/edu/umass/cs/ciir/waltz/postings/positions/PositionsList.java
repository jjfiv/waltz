package edu.umass.cs.ciir.waltz.postings.positions;

import ciir.jfoley.chai.collections.list.BitVector;
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

  default void fill(BitVector vec) {
    int vsize = vec.size();
    for (int index = 0; index < size(); index++) {
      int pos = getPosition(index);
      if(pos >= vsize) break;
      vec.set(pos);
    }
  }

  default Integer get(int index) {
    return getPosition(index);
  }

}
