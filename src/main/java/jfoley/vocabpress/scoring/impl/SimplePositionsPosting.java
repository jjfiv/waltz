package jfoley.vocabpress.scoring.impl;

import jfoley.vocabpress.scoring.PositionsPosting;

import java.util.AbstractList;
import java.util.List;

/**
 * @author jfoley
 */
public class SimplePositionsPosting extends SimplePosting implements PositionsPosting {
  public final List<Integer> positions;

  public SimplePositionsPosting(int document, List<Integer> positions) {
    super(document);
    this.positions = positions;
  }

  @Override
  public List<Integer> getPositions() {
    return positions;
  }

  @Override
  public int getCount() {
    return positions.size();
  }
}
