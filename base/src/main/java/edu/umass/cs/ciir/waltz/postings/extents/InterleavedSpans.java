package edu.umass.cs.ciir.waltz.postings.extents;

import ciir.jfoley.chai.collections.list.AChaiList;
import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.postings.extents.iter.ListSpanIterator;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Hooray for premature optimization -- this only stores an int[], not a Object[] and a bunch of pointers.
 * @author jfoley
 */
public class InterleavedSpans extends AChaiList<Span> implements SpanList {
  private IntList data;

  public InterleavedSpans() {
    this.data = new IntList();
  }
  public InterleavedSpans(@Nonnull Collection<? extends Span> original) {
    this();
    for (Span extent : original) {
      add(extent);
    }
  }

  @Override
  public boolean add(@Nonnull Span extent) {
    assert(extent.begin >= 0);
    assert(extent.end >= 0);
    assert(extent.begin < extent.end);
    this.data.add(extent.begin);
    this.data.add(extent.end);
    return true;
  }

  /**
   * This is the same as add, but avoids boxing.
   * @param begin integer begin.
   * @param end integer end.
   */
  @Override
  public void push(int begin, int end) {
    assert(begin >= 0);
    assert(end >= 0);
    assert(begin < end);
    this.data.add(begin);
    this.data.add(end);
  }

  @Nonnull
  @Override
  public Span get(int index) {
    int off = index * 2;
    return new Span(data.get(off), data.get(off+1));
  }

  @Override
  public void set(int index, int begin, int end) {
    int off = index * 2;
    data.set(off, begin);
    data.set(off+1, end);
  }

  @Override
  public Span set(int index, Span extent) {
    int off = index * 2;
    int oldBegin = data.set(off, extent.begin);
    int oldEnd = data.set(off+1, extent.end);
    return new Span(oldBegin, oldEnd);
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
  public SpanIterator getSpanIterator() {
    return new ListSpanIterator(this);
  }

  @Override
  public int size() {
    return data.size() / 2;
  }
}
