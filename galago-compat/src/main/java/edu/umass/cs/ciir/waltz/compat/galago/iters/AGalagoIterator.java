package edu.umass.cs.ciir.waltz.compat.galago.iters;

import edu.umass.cs.ciir.waltz.dociter.movement.Mover;
import org.lemurproject.galago.core.retrieval.iterator.BaseIterator;
import org.lemurproject.galago.core.retrieval.processing.ScoringContext;
import org.lemurproject.galago.core.retrieval.query.AnnotatedNode;

import java.io.IOException;

/**
 * @author jfoley
 */
public class AGalagoIterator<MoverKind extends Mover> implements BaseIterator {
  public MoverKind mover;
  public AGalagoIterator(MoverKind mover) {
    this.mover = mover;
  }

  @Override
  public void reset() throws IOException {
    mover.reset();
  }

  @Override
  public long currentCandidate() {
    return mover.currentKey();
  }

  @Override
  public boolean isDone() {
    return mover.isDone();
  }

  @Override
  public void movePast(long identifier) throws IOException {
    mover.movePast((int) identifier);
  }

  @Override
  public void syncTo(long identifier) throws IOException {
    mover.movePast((int) identifier);
  }

  @Override
  public boolean hasMatch(long identifier) {
    return mover.matches((int) identifier);
  }

  @Override
  public boolean hasAllCandidates() {
    return false;
  }

  @Override
  public long totalEntries() {
    return mover.totalKeys();
  }

  @Override
  public String getValueString(ScoringContext sc) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public AnnotatedNode getAnnotatedNode(ScoringContext sc) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int compareTo(BaseIterator o) {
    return Long.compare(this.currentCandidate(), o.currentCandidate());
  }
}

