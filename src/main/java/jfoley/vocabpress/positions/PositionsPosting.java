package jfoley.vocabpress.positions;

import jfoley.vocabpress.extents.ExtentsIterator;
import jfoley.vocabpress.positions.PositionsList;
import jfoley.vocabpress.postings.CountPosting;

/**
 * A positions posting has a document, a count, and a positions array.
 * @author jfoley
 */
public interface PositionsPosting extends CountPosting {
  /** return a reference to the positions array */
  public PositionsList getPositions();
  /** return an ExtentsIterator of the positions array */
  public ExtentsIterator getExtentsIterator();
}
