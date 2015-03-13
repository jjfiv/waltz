package jfoley.vocabpress.postings.impl;

import jfoley.vocabpress.postings.APosting;
import jfoley.vocabpress.postings.extents.ExtentIterable;
import jfoley.vocabpress.postings.extents.ExtentsAsPositions;
import jfoley.vocabpress.postings.extents.ExtentsIterator;
import jfoley.vocabpress.postings.extents.ExtentsList;
import jfoley.vocabpress.postings.extents.iter.ListExtentsIterator;
import jfoley.vocabpress.postings.positions.PositionsList;

/**
 * @author jfoley
 */
public class SimpleExtentsPosting extends APosting<ExtentsList> implements ExtentIterable {
  private final ExtentsList extents;

  protected SimpleExtentsPosting(int document, ExtentsList extents) {
    super(document);
    this.extents = extents;
  }

  public ExtentsList getExtents() {
    return extents;
  }

  public ExtentsIterator getExtentsIterator() {
    return new ListExtentsIterator(extents);
  }

  public PositionsList getPositions() {
    return new ExtentsAsPositions(extents);
  }

  public int getCount() {
    return extents.size();
  }

  @Override
  public ExtentsList getValue() {
    return getExtents();
  }
}
