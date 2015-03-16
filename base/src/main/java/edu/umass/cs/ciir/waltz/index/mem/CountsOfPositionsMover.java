package edu.umass.cs.ciir.waltz.index.mem;

import edu.umass.cs.ciir.waltz.dociter.movement.MappingMover;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;

/**
 * @author jfoley
 */
public class CountsOfPositionsMover extends MappingMover<PositionsList, Integer> {
  public CountsOfPositionsMover(PostingMover<PositionsList> plist) {
    super(plist);
  }

  @Override
  public Integer getCurrentPosting() {
    return inner.getCurrentPosting().size();
  }
}
