package edu.umass.cs.ciir.waltz.dociter.movement;

import ciir.jfoley.chai.fn.TransformFn;
import edu.umass.cs.ciir.waltz.sys.KeyMetadata;

import javax.annotation.Nullable;

/**
 * @author jfoley
 */
public class IdMappingMover<X> implements PostingMover<X> {
  private final Mover inner;
  private final TransformFn<Integer, X> mapper;

  public IdMappingMover(Mover inner, TransformFn<Integer,X> mapper) {
    this.inner = inner;
    this.mapper = mapper;
  }
  @Override
  @Nullable
  public KeyMetadata<X> getMetadata() {
    return null;
  }

  @Override
  public X getCurrentPosting() {
    return mapper.transform(this.currentKey());
  }

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

  @Override
  public int totalKeys() {
    return inner.totalKeys();
  }
}
