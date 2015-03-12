package jfoley.vocabpress.postings.extents;

import java.util.List;

/**
 * @author jfoley
 */
public interface ExtentsList extends List<Extent>, ExtentIterable {
  int getBegin(int index);
  int getEnd(int index);
}