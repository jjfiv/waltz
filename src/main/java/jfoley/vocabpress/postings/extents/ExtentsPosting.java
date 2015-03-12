package jfoley.vocabpress.postings.extents;

import jfoley.vocabpress.postings.positions.PositionsPosting;

/**
 * @author jfoley
 */
public interface ExtentsPosting extends PositionsPosting {
  public ExtentsList getExtents();
}
