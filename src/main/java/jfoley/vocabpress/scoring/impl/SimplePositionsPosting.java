package jfoley.vocabpress.scoring.impl;

import ciir.jfoley.chai.collections.list.IntList;
import jfoley.vocabpress.scoring.PositionsPosting;

import java.util.List;

/**
 * @author jfoley
 */
public class SimplePositionsPosting extends SimplePosting implements PositionsPosting {
  public final IntList positions;

  public SimplePositionsPosting(int document, List<Integer> positions) {
    super(document);
    this.positions = new IntList(positions);
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
