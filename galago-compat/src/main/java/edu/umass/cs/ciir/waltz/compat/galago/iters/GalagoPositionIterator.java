package edu.umass.cs.ciir.waltz.compat.galago.iters;

import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import org.lemurproject.galago.core.retrieval.iterator.ExtentIterator;
import org.lemurproject.galago.core.retrieval.processing.ScoringContext;
import org.lemurproject.galago.core.util.ExtentArray;

/**
 * @author jfoley
 */
public class GalagoPositionIterator extends AGalagoIterator<PostingMover<PositionsList>> implements ExtentIterator {

  ExtentArray extents;
  public GalagoPositionIterator(PostingMover<PositionsList> mover) {
    super(mover);
    extents = new ExtentArray();
  }

  @Override
  public ExtentArray extents(ScoringContext c) {
    if(mover.matches((int) c.document)) {
      if (extents.getDocument() != c.document) {
        extents.reset();
        extents.setDocument(c.document);
        PositionsList pl = mover.getCurrentPosting();
        for (int i = 0; i < pl.size(); i++) {
          extents.add(pl.getPosition(i));
        }
      }
      return extents;
    }
    return ExtentArray.EMPTY;
  }

  @Override
  public int count(ScoringContext c) {
    if(mover.matches((int) c.document)) {
      return mover.getCurrentPosting().size();
    }
    return 0;
  }

  @Override
  public ExtentArray data(ScoringContext c) {
    return extents(c);
  }
}
