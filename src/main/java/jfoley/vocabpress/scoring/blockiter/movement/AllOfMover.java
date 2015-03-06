package jfoley.vocabpress.scoring.blockiter.movement;

import java.util.List;

/**
 * This is "AND" style movement, where all lists must contain a given document for it to be considered a match.
 * @author jfoley
 */
public class AllOfMover implements Mover {
  private final List<Mover> children;

  public AllOfMover(List<Mover> children) {
    this.children = children;
    findMatch();
  }

  private int findNextPossibleMatch() {
    int maxCandidate = 0;
    for (Mover child : children) {
      maxCandidate = Math.max(maxCandidate, child.currentKey());
    }
    return maxCandidate;
  }

  private int findMatch() {
    int maybeMatch = findNextPossibleMatch();
    while(maybeMatch != DONE_ID) {
      // Attempt to move all iterators to the possible match.
      moveTo(maybeMatch);

      // Find the lowest possible match from this new position.
      int newBest = findNextPossibleMatch();

      // If it's the same, then it's a real match.
      if(newBest == maybeMatch) {
        return newBest;
      }

      // Otherwise, try the next possible match.
      maybeMatch = newBest;
    }
    return DONE_ID;
  }

  @Override
  public int maxKey() {
    int max = 0;
    for (Mover child : children) {
      max = Math.max(max, child.maxKey());
    }
    return max;
  }

  @Override
  public int currentKey() {
    return findMatch();
  }

  @Override
  public boolean isDone() {
    return currentKey() == DONE_ID;
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
    moveTo(key+1);
  }

  @Override
  public int compareTo(Mover o) {
    return Integer.compare(currentKey(), o.currentKey());
  }
}
