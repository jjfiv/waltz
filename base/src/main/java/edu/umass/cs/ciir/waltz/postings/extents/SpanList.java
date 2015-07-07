package edu.umass.cs.ciir.waltz.postings.extents;

import java.util.List;

/**
 * A sorted list of [begin,end) pairs.
 * @author jfoley
 */
public interface SpanList extends List<Span>, SpanIterable {
  /**
   * Add an extent into this list, more efficiently than add().
   * @param begin start position.
   * @param end end position.
   */
  void push(int begin, int end);
  /**
   * Updated an extent in this list, more efficiently than Extent set(Extent).
   * @param index which extent to replace.
   * @param begin start position.
   * @param end end position.
   */
  void set(int index, int begin, int end);

  int getBegin(int index);
  int getEnd(int index);

  default boolean contains(int x) {
    for (int i = 0; i < this.size(); i++) {
      // optimization because it's sorted
      if(x < this.getBegin(i)) {
        return false;
      }
      if(x >= this.getBegin(i) && x < this.getEnd(i)) {
        return true;
      }
    }
    return false;
  }
}
