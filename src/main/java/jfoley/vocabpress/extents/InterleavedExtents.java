package jfoley.vocabpress.extents;

import ciir.jfoley.chai.collections.list.IntList;
import jfoley.vocabpress.postings.Extent;

import java.util.AbstractList;
import java.util.List;

/**
 * @author jfoley
 */
public class InterleavedExtents extends AbstractList<Extent> implements ExtentsList {
  private IntList data;

  public InterleavedExtents(List<Extent> original) {
    for (Extent extent : original) {
      add(extent);
    }
  }

  @Override
  public boolean add(Extent extent) {
    this.data.add(extent.begin);
    this.data.add(extent.end);
    return true;
  }

  @Override
  public Extent get(int index) {
    int off = index / 2;
    return new Extent(data.get(off), data.get(off+1));
  }

  @Override
  public int getBegin(int index) {
    return data.get(index / 2);
  }

  @Override
  public int getEnd(int index) {
    return data.get((index / 2) + 1);
  }

  @Override
  public ExtentsIterator getIterator() {
    return new ListExtentsIterator(this);
  }

  @Override
  public int size() {
    return data.size() / 2;
  }
}
