package jfoley.vocabpress.postings.impl;

import ciir.jfoley.chai.collections.list.IntList;
import jfoley.vocabpress.extents.ExtentsIterator;
import jfoley.vocabpress.extents.iter.IntArrayPosIter;
import jfoley.vocabpress.positions.PositionsList;
import jfoley.vocabpress.positions.PositionsPosting;
import jfoley.vocabpress.positions.SimplePositionsList;
import jfoley.vocabpress.postings.APosting;

import java.util.List;

/**
 * @author jfoley
 */
public class SimplePositionsPosting extends APosting implements PositionsPosting {
  public final IntList positions;

  public SimplePositionsPosting(int document, List<Integer> positions) {
    super(document);
    this.positions = new IntList(positions);
  }

  @Override
  public PositionsList getPositions() {
    return new SimplePositionsList(positions);
  }

  @Override
  public ExtentsIterator getExtentsIterator() {
    return new IntArrayPosIter(positions.unsafeArray(), positions.size());
  }

  @Override
  public int getCount() {
    return positions.size();
  }

  @Override
  public String toString() {
    return "Doc: "+this.getKey()+". Pos: "+positions.toString();
  }
}
