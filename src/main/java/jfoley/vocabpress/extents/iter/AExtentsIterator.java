package jfoley.vocabpress.extents.iter;

import jfoley.vocabpress.extents.ExtentsIterator;

/**
 * @author jfoley
 */
public abstract class AExtentsIterator implements ExtentsIterator {
  protected int size;
  protected int pos;

  public AExtentsIterator(int size) {
    this.pos = 0;
    this.size = size;
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
}
