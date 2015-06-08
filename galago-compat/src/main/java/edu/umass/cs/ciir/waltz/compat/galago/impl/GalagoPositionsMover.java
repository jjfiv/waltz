package edu.umass.cs.ciir.waltz.compat.galago.impl;

import ciir.jfoley.chai.collections.list.AChaiList;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsIterator;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.statistics.PostingListStatistics;
import org.lemurproject.galago.core.retrieval.iterator.ExtentIterator;
import org.lemurproject.galago.core.util.ExtentArray;

import javax.annotation.Nullable;

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

  public static class GalagoExtentsAsPositionsList extends AChaiList<Integer> implements PositionsList {
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
    public PositionsIterator getSpanIterator() {
      return new PositionsIterator(this);
    }

    @Override
    public Integer get(int index) {
      return getPosition(index);
    }
  }
}
