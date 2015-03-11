package jfoley.vocabpress.extents;

import jfoley.vocabpress.scoring.Extent;

import java.util.AbstractList;
import java.util.List;

/**
* @author jfoley
*/
public class PositionsView extends AbstractList<Integer> {
  private final List<Extent> extents;

  public PositionsView(List<Extent> extents) {
    this.extents = extents;
  }

  @Override
  public Integer get(int index) {
    return extents.get(index).begin;
  }

  @Override
  public int size() {
    return extents.size();
  }
}
