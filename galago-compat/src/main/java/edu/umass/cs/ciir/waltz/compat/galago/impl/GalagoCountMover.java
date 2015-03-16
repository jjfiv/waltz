package edu.umass.cs.ciir.waltz.compat.galago.impl;

import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import org.lemurproject.galago.core.retrieval.iterator.CountIterator;

/**
 * @author jfoley
 */
public class GalagoCountMover extends AGalagoMover<CountIterator> implements PostingMover<Integer> {
  public GalagoCountMover(CountIterator iter) {
    super(iter);
  }

  @Override
  public Integer getCurrentPosting() {
    ctx.document = currentKey();
    return iter.count(ctx);
  }
}
