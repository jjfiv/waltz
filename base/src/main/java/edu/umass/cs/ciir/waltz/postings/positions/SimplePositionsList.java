package edu.umass.cs.ciir.waltz.postings.positions;

import ciir.jfoley.chai.collections.util.ArrayFns;

import java.util.Collections;
import java.util.List;

/**
 * @author jfoley
 */
public class SimplePositionsList implements PositionsList {
  private final List<Integer> data;

  public SimplePositionsList(List<Integer> inner) {
    assert(inner != null);
    this.data = inner;
  }

  @Override
  public int size() {
    return data.size();
  }

  @Override
  public int getPosition(int index) {
    return data.get(index);
  }

  @Override
  public PositionsIterator getExtentsIterator() {
    return new PositionsIterator(this);
  }

  @Override
  public List<Integer> toList() {
    return Collections.unmodifiableList(data);
  }

  public static SimplePositionsList of(int... pos) {
    return new SimplePositionsList(ArrayFns.toList(pos));
  }
}
