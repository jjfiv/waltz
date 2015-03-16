package edu.umass.cs.ciir.waltz.index.mem;

import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;

/**
 * @author jfoley
 */
public class CountsOfPositionsMover implements PostingMover<Integer> {
  private final PostingMover<PositionsList> inner;

  public CountsOfPositionsMover(PostingMover<PositionsList> plist) {
    this.inner = plist;
  }

  @Override
  public Integer getCurrentPosting() {
    return inner.getCurrentPosting().size();
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
}
