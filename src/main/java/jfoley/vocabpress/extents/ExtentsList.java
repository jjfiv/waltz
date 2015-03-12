package jfoley.vocabpress.extents;

import jfoley.vocabpress.scoring.Extent;

import java.util.List;

/**
 * @author jfoley
 */
public interface ExtentsList extends List<Extent> {
  int getBegin(int index);
  int getEnd(int index);
  ExtentsIterator getIterator();
}
