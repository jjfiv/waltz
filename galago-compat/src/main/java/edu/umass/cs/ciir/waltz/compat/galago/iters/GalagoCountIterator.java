package edu.umass.cs.ciir.waltz.compat.galago.iters;

import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import org.lemurproject.galago.core.retrieval.iterator.CountIterator;
import org.lemurproject.galago.core.retrieval.iterator.LengthsIterator;
import org.lemurproject.galago.core.retrieval.processing.ScoringContext;

/**
 * @author jfoley
 */
public class GalagoCountIterator extends AGalagoIterator<PostingMover<Integer>> implements CountIterator, LengthsIterator {
  public GalagoCountIterator(PostingMover<Integer> intMover) {
    super(intMover);
  }

  @Override
  public int count(ScoringContext c) {
    if(mover.matches((int) c.document)) {
      return mover.getCurrentPosting();
    }
    return 0;
  }

  @Override
  public boolean indicator(ScoringContext c) {
    return count(c) > 0;
  }

  @Override
  public int length(ScoringContext c) {
    return count(c);
  }
}
