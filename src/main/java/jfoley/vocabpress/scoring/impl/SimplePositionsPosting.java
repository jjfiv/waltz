package jfoley.vocabpress.scoring.impl;

import jfoley.vocabpress.scoring.PositionsArray;
import jfoley.vocabpress.scoring.PositionsPosting;

import java.util.AbstractList;
import java.util.List;

/**
 * @author jfoley
 */
public class SimplePositionsPosting implements PositionsPosting {
  public final int document;
  public final SparsePositionsArray positions;

  public SimplePositionsPosting(int document, List<Integer> positions) {
    this.document = document;
    this.positions = new SparsePositionsArray(positions);
  }

  @Override
  public PositionsArray getPositions() {
    return positions;
  }

  @Override
  public int getCount() {
    return positions.size();
  }

  @Override
  public int getKey() {
    return document;
  }
}
