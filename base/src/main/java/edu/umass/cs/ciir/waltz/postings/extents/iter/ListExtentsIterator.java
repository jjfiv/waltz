package edu.umass.cs.ciir.waltz.postings.extents.iter;

import edu.umass.cs.ciir.waltz.postings.extents.ExtentsList;

/**
* @author jfoley
*/
public class ListExtentsIterator extends AExtentsIterator {
  private final ExtentsList extents;

  public ListExtentsIterator(ExtentsList extents) {
    super(extents.size());
    this.extents = extents;
  }

  @Override
  public int currentBegin() {
    return extents.get(pos).begin;
  }

  @Override
  public int currentEnd() {
    return extents.get(pos).end;
  }
}
