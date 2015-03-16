package edu.umass.cs.ciir.waltz.compat.galago.impl;

import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsIterator;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import org.lemurproject.galago.core.retrieval.iterator.ExtentIterator;
import org.lemurproject.galago.core.util.ExtentArray;

import java.util.AbstractList;
import java.util.List;

/**
 * @author jfoley
 */
public class GalagoPositionsMover extends AGalagoMover<ExtentIterator> implements PostingMover<PositionsList> {
  public GalagoPositionsMover(ExtentIterator forTerm) {
    super(forTerm);
  }

  @Override
  public PositionsList getCurrentPosting() {
    ctx.document = currentKey();
    ExtentArray arr = iter.extents(ctx);
    return new GalagoExtentsAsPositionsList(arr);
  }

  public static class GalagoExtentsAsPositionsList extends AbstractList<Integer> implements PositionsList {
    private final ExtentArray arr;

    public GalagoExtentsAsPositionsList(ExtentArray arr) {
      this.arr = arr;
    }

    @Override
    public int getPosition(int index) {
      return arr.begin(index);
    }

    @Override
    public int size() {
      return arr.size();
    }

    @Override
    public PositionsIterator getExtentsIterator() {
      return new PositionsIterator(this);
    }

    @Override
    public List<Integer> toList() {
      return this;
    }

    @Override
    public Integer get(int index) {
      return getPosition(index);
    }
  }
}
