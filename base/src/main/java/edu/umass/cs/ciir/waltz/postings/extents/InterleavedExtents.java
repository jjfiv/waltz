package edu.umass.cs.ciir.waltz.postings.extents;

import ciir.jfoley.chai.collections.list.AChaiList;
import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.postings.extents.iter.ListExtentsIterator;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Hooray for premature optimization -- this only stores an int[], not a Object[] and a bunch of pointers.
 * @author jfoley
 */
public class InterleavedExtents extends AChaiList<Extent> implements ExtentsList {
  private IntList data;

  public InterleavedExtents() {
    this.data = new IntList();
  }
  public InterleavedExtents(@Nonnull List<Extent> original) {
    this();
    for (Extent extent : original) {
      add(extent);
    }
  }

  @Override
  public boolean add(@Nonnull Extent extent) {
    assert(extent.begin >= 0);
    assert(extent.end >= 0);
    assert(extent.begin < extent.end);
    this.data.add(extent.begin);
    this.data.add(extent.end);
    return true;
  }

  public void push(int begin, int end) {
    assert(begin >= 0);
    assert(end >= 0);
    assert(begin < end);
    this.data.add(begin);
    this.data.add(end);
  }

  @Nonnull
  @Override
  public Extent get(int index) {
    int off = index * 2;
    return new Extent(data.get(off), data.get(off+1));
  }

  @Override
  public int getBegin(int index) {
    return data.get(index * 2);
  }

  @Override
  public int getEnd(int index) {
    return data.get((index * 2) + 1);
  }

  @Override
  @Nonnull
  public ExtentsIterator getExtentsIterator() {
    return new ListExtentsIterator(this);
  }

  @Override
  public int size() {
    return data.size() / 2;
  }
}
