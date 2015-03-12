package jfoley.vocabpress.compat.galago;

import jfoley.vocabpress.dociter.movement.PostingMover;
import jfoley.vocabpress.postings.CountPosting;
import jfoley.vocabpress.postings.impl.SimpleCountPosting;
import org.lemurproject.galago.core.retrieval.iterator.CountIterator;

/**
 * @author jfoley
 */
public class GalagoCountMover extends AGalagoMover<CountIterator> implements PostingMover<CountPosting> {
  public GalagoCountMover(CountIterator iter) {
    super(iter);
  }

  @Override
  public CountPosting getCurrentPosting() {
    ctx.document = currentKey();
    return new SimpleCountPosting(currentKey(), iter.count(ctx));
  }
}
