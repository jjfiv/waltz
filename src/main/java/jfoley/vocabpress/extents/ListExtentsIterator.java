package jfoley.vocabpress.extents;

import jfoley.vocabpress.postings.Extent;

import java.util.List;

/**
* @author jfoley
*/
public class ListExtentsIterator implements ExtentsIterator {
  private final List<Extent> extents;
  private final int size;
  private int pos;

  public ListExtentsIterator(List<Extent> extents) {
    this.extents = extents;
    this.pos = 0;
    this.size = extents.size();
  }

  @Override
  public boolean isDone() {
    return pos >= size;
  }

  @Override
  public boolean next() {
    pos++;
    return !isDone();
  }

  @Override
  public void reset() {
    pos = 0;
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
