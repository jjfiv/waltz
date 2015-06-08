package edu.umass.cs.ciir.waltz.dociter.movement;

import edu.umass.cs.ciir.waltz.postings.Posting;
import edu.umass.cs.ciir.waltz.statistics.PostingListStatistics;

/**
 * @author jfoley
 */
public class EmptyMover<X extends Posting> extends AMover implements PostingMover<X> {
  public static <Y extends Posting> EmptyMover<Y> getInstance() {
    return new EmptyMover<>();
  }

  @Override public void nextBlock() { }
  @Override public void reset() {  }
  @Override public int totalKeys() { return 0; }
  @Override public X getCurrentPosting() { return null; }
}
