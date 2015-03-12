package jfoley.vocabpress.compat.galago;

import jfoley.vocabpress.dociter.movement.Mover;
import org.lemurproject.galago.core.retrieval.iterator.BaseIterator;
import org.lemurproject.galago.core.retrieval.processing.ScoringContext;

import java.io.IOException;

/**
 * @author jfoley
 */
public class AGalagoMover<X extends BaseIterator> implements Mover {
  protected final X iter;
  public final ScoringContext ctx;

  public AGalagoMover(X iter) {
    this.iter = iter;
    this.ctx = new ScoringContext();
  }

  @Override
  public int maxKey() {
    return (int) iter.currentCandidate();
  }

  @Override
  public int currentKey() {
    return (int) iter.currentCandidate();
  }

  @Override
  public boolean isDone() {
    return !iter.isDone();
  }

  @Override
  public void next() {
    try {
      iter.movePast(iter.currentCandidate());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isDoneWithBlock() {
    return false;
  }

  @Override
  public void nextBlock() {
    // Nothing.
  }

  @Override
  public void nextKey() {
    next();
  }

  @Override
  public void moveTo(int key) {
    try {
      iter.syncTo(key);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void movePast(int key) {
    try {
      iter.movePast(key);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void rewindBlock(int key) {
    // Nothing.
  }

  @Override
  public void reset() {
    try {
      iter.reset();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean matches(int key) {
    return iter.hasMatch(key);
  }

  @Override
  public void moveToAbsolute(int key) {
    int currentKey = currentKey();
    if(key == currentKey) return;

    if(key > currentKey) {
      moveTo(key);
    } else  if(key < currentKey()) {
      // TODO log a warning about slow resets needed.
      reset();
      moveTo(key);
    }
  }
}
