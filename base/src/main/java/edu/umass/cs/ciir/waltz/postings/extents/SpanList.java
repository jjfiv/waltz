package edu.umass.cs.ciir.waltz.postings.extents;

import java.util.List;

/**
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
}
