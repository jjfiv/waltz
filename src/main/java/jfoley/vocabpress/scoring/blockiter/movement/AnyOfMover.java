package jfoley.vocabpress.scoring.blockiter.movement;

import ciir.jfoley.chai.collections.util.ListFns;

import java.util.List;

/**
 * This implements "OR" style movement, where any child that has any document involves that document being scored.
 * @author jfoley
 */
public class AnyOfMover implements Mover {
  private final List<Mover> children;

  public AnyOfMover(List<Mover> movers) {
    this.children = ListFns.ensureRandomAccess(movers);
  }

  @Override
  public int maxKey() {
    int max = Integer.MIN_VALUE;
    for (Mover child : children) {
      max = Math.max(max, child.maxKey());
    }
    return max;
  }

  @Override
  public int currentKey() {
    int min = Integer.MAX_VALUE;
    for (Mover child : children) {
      min = Math.min(min, child.currentKey());
    }
    return min;
  }

  /** returns true if all are done */
  @Override
  public boolean isDone() {
    for (Mover child : children) {
      if(!child.isDone()) return false;
    }
    return true;
  }

  @Override
  public void next() {
    movePast(currentKey());
  }

  @Override
  public void moveTo(int key) {
    for (Mover child : children) {
      child.moveTo(key);
    }
  }

  @Override
  public void movePast(int key) {
    for (Mover child : children) {
      child.movePast(key);
    }
  }

  @Override
  public int compareTo(Mover o) {
    return Integer.compare(currentKey(), o.currentKey());
  }
}
