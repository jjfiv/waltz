package jfoley.vocabpress.scoring.impl;

import jfoley.vocabpress.scoring.PositionsArray;

import java.util.AbstractList;
import java.util.List;

/**
 * @author jfoley
 */
public class SparsePositionsArray extends AbstractList<Integer> implements PositionsArray {
  private final List<Integer> positions;

  public SparsePositionsArray(List<Integer> actualPositions) {
    this.positions = actualPositions;
  }

  @Override
  public Integer get(int index) {
    return positions.get(index);
  }

  @Override
  public int size() {
    return positions.size();
  }
}
