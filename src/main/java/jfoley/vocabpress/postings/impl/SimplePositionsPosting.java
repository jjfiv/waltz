package jfoley.vocabpress.postings.impl;

import ciir.jfoley.chai.collections.list.IntList;
import jfoley.vocabpress.postings.APosting;
import jfoley.vocabpress.postings.extents.ExtentsIterator;
import jfoley.vocabpress.postings.extents.iter.IntArrayPosIter;
import jfoley.vocabpress.postings.positions.PositionsList;
import jfoley.vocabpress.postings.positions.SimplePositionsList;

import java.util.List;

/**
 * @author jfoley
 */
public class SimplePositionsPosting extends APosting<PositionsList> {
  public final IntList positions;

  public SimplePositionsPosting(int document, List<Integer> positions) {
    super(document);
    this.positions = new IntList(positions);
  }

  public PositionsList getPositions() {
    return new SimplePositionsList(positions);
  }

  public ExtentsIterator getExtentsIterator() {
    return new IntArrayPosIter(positions.unsafeArray(), positions.size());
  }

  public int getCount() {
    return positions.size();
  }

  @Override
  public String toString() {
    return "Doc: "+this.getKey()+". Pos: "+positions.toString();
  }

  @Override
  public PositionsList getValue() {
    return new SimplePositionsList(positions);
  }
}
