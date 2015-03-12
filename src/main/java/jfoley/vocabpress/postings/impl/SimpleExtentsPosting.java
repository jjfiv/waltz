package jfoley.vocabpress.postings.impl;

import jfoley.vocabpress.postings.extents.ExtentsAsPositions;
import jfoley.vocabpress.postings.extents.ExtentsIterator;
import jfoley.vocabpress.postings.extents.ExtentsList;
import jfoley.vocabpress.postings.extents.iter.ListExtentsIterator;
import jfoley.vocabpress.postings.APosting;
import jfoley.vocabpress.postings.extents.ExtentsPosting;
import jfoley.vocabpress.postings.positions.PositionsList;

/**
 * @author jfoley
 */
public class SimpleExtentsPosting extends APosting implements ExtentsPosting {
  private final ExtentsList extents;

  protected SimpleExtentsPosting(int document, ExtentsList extents) {
    super(document);
    this.extents = extents;
  }

  @Override
  public ExtentsList getExtents() {
    return extents;
  }

  @Override
  public ExtentsIterator getExtentsIterator() {
    return new ListExtentsIterator(extents);
  }

  @Override
  public PositionsList getPositions() {
    return new ExtentsAsPositions(extents);
  }

  @Override
  public int getCount() {
    return extents.size();
  }

}
