package jfoley.vocabpress.scoring.blockiter.movement;

import ciir.jfoley.chai.collections.util.ListFns;

import java.util.Arrays;
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

  public static AnyOfMover of(Mover... children) {
    return new AnyOfMover(Arrays.asList(children));
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
    // In case one of our children is an AnyOf, we ask it to estimate a hit:
    // This gives us the "short-circuit" evaluation of currentKey that we expect from languages.
    int minEstimated = estimateKeyLowerBound();

    // We know that this is a lower-bound on the key to expect.
    int min = minEstimated;
    for (Mover child : children) {
      min = Math.min(min, child.currentKey());
      // if we found a key that is our estimate, short-circuit the evaluation of currentKey();
      if(min == minEstimated) return min;
    }
    return min;
  }

  @Override
  public int estimateKeyLowerBound() {
    int min = Integer.MAX_VALUE;
    for (Mover child : children) {
      min = Math.min(min, child.estimateKeyLowerBound());
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
