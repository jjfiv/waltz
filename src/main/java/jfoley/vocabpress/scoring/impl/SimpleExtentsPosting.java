package jfoley.vocabpress.scoring.impl;

import jfoley.vocabpress.extents.BeginsView;
import jfoley.vocabpress.extents.ExtentsIterator;
import jfoley.vocabpress.extents.ExtentsList;
import jfoley.vocabpress.extents.ListExtentsIterator;
import jfoley.vocabpress.scoring.ExtentsPosting;

import java.util.List;

/**
 * @author jfoley
 */
public class SimpleExtentsPosting extends SimplePosting implements ExtentsPosting {
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
  public List<Integer> getPositions() {
    return new BeginsView(extents);
  }

  @Override
  public int getCount() {
    return extents.size();
  }

}
