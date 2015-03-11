package jfoley.vocabpress.scoring.impl;

import jfoley.vocabpress.extents.ListExtentsIterator;
import jfoley.vocabpress.extents.PositionsView;
import jfoley.vocabpress.extents.ExtentsIterator;
import jfoley.vocabpress.scoring.Extent;
import jfoley.vocabpress.scoring.ExtentsPosting;

import java.util.List;

/**
 * @author jfoley
 */
public class SimpleExtentsPosting extends SimplePosting implements ExtentsPosting {
  private final List<Extent> extents;

  protected SimpleExtentsPosting(int document, List<Extent> extents) {
    super(document);
    this.extents = extents;
  }

  @Override
  public List<Extent> getExtents() {
    return extents;
  }

  @Override
  public ExtentsIterator getExtentsIterator() {
    return new ListExtentsIterator(extents);
  }

  @Override
  public List<Integer> getPositions() {
    return new PositionsView(extents);
  }

  @Override
  public int getCount() {
    return extents.size();
  }

}
