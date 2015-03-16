package edu.umass.cs.ciir.waltz.dociter.movement;

/**
 * This is a mover that wraps another mover to deliver output of a different kind, but doesn't change movement rules at all.
 * e.g. positions -> counts
 * @author jfoley
 */
public abstract class MappingMover<A, B> implements PostingMover<B> {
  protected final PostingMover<A> inner;
  public MappingMover(PostingMover<A> inner) {
    this.inner = inner;
  }

  @Override
  public abstract B getCurrentPosting();

  @Override
  public int maxKey() {
    return inner.maxKey();
  }

  @Override
  public int currentKey() {
    return inner.currentKey();
  }

  @Override
  public boolean isDone() {
    return inner.isDone();
  }

  @Override
  public void next() {
    inner.next();
  }

  @Override
  public boolean isDoneWithBlock() {
    return inner.isDoneWithBlock();
  }

  @Override
  public void nextBlock() {
    inner.nextBlock();
  }

  @Override
  public void nextKey() {
    inner.nextKey();
  }

  @Override
  public void moveTo(int key) {
    inner.moveTo(key);
  }

  @Override
  public void movePast(int key) {
    inner.movePast(key);
  }

  @Override
  public void rewindBlock(int key) {
    inner.rewindBlock(key);
  }

  @Override
  public void reset() {
    inner.reset();
  }

  @Override
  public boolean matches(int key) {
    return inner.matches(key);
  }

  @Override
  public void moveToAbsolute(int key) {
    inner.moveToAbsolute(key);
  }
}
