package jfoley.vocabpress.scoring.impl;

import jfoley.vocabpress.phrase.ExtentsIterator;
import jfoley.vocabpress.scoring.Extent;
import jfoley.vocabpress.scoring.ExtentsPosting;

import java.util.AbstractList;
import java.util.List;

/**
 * @author jfoley
 */
public class SimpleExtentsPosting extends SimplePosting implements ExtentsPosting {
  private final List<Extent> extents;

  protected SimpleExtentsPosting(int document, List<Extent> extents) {
    super(document);
    this.extents = extents;
  }

  @Override
  public List<Extent> getExtents() {
    return extents;
  }

  @Override
  public ExtentsIterator getExtentsIterator() {
    return new ListExtentsIterator(extents);
  }

  @Override
  public List<Integer> getPositions() {
    return new PositionsView(extents);
  }

  @Override
  public int getCount() {
    return extents.size();
  }

  public static class PositionsView extends AbstractList<Integer> {
    private final List<Extent> extents;

    public PositionsView(List<Extent> extents) {
      this.extents = extents;
    }

    @Override
    public Integer get(int index) {
      return extents.get(index).begin;
    }

    @Override
    public int size() {
      return extents.size();
    }
  }

  public static class ListExtentsIterator implements ExtentsIterator {
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
    public int currentBegin() {
      return extents.get(pos).begin;
    }

    @Override
    public int currentEnd() {
      return extents.get(pos).end;
    }
  }
}
