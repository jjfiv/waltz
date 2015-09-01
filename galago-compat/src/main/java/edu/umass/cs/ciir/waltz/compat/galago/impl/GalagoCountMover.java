package edu.umass.cs.ciir.waltz.compat.galago.impl;

import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.sys.KeyMetadata;
import org.lemurproject.galago.core.retrieval.iterator.CountIterator;

import javax.annotation.Nullable;

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

  @Nullable
  @Override
  public KeyMetadata<Integer> getMetadata() {
    return null;
  }
}
