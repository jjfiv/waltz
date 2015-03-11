package jfoley.vocabpress.scoring;

import jfoley.vocabpress.extents.ExtentsIterator;

import java.util.List;

/**
 * A positions posting has a document, a count, and a positions array.
 * @author jfoley
 */
public interface PositionsPosting extends CountPosting {
  /** return a reference to the positions array */
  public List<Integer> getPositions();
  /** return an ExtentsIterator of the positions array */
  public ExtentsIterator getExtentsIterator();
}
