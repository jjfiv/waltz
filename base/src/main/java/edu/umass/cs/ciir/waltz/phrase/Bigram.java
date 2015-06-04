package edu.umass.cs.ciir.waltz.phrase;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsIterator;
import edu.umass.cs.ciir.waltz.postings.positions.SimplePositionsList;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;

/**
 * @author jfoley
 */
public class Bigram {

  /**
   * Return the count of occurences of a bigram.
   * @param leftP the positions of the first term in the bigram.
   * @param rightP the positions of the second term in the bigram
   */
  public static int count(PositionsList leftP, PositionsList rightP) {
    int hits = 0;
    int leftIndex = 0;
    int rightIndex = 0;
    while(leftIndex < leftP.size() && rightIndex < rightP.size()) {
      while(leftP.getPosition(leftIndex) < rightP.getPosition(rightIndex) -1) {
        if(++leftIndex >= leftP.size()) return hits;
      }
      while(rightP.getPosition(rightIndex) < leftP.getPosition(leftIndex) +1) {
        if(++rightIndex >= rightP.size()) return hits;
      }
      if(leftP.getPosition(leftIndex) +1 == rightP.getPosition(rightIndex)) {
        hits++;
        leftIndex++;
        rightIndex++;
      }
    }
    return hits;
  }

  public static PositionsList positions(PositionsList leftP, PositionsList rightP) {
    IntList hits = new IntList(Math.min(leftP.size(), rightP.size()));
    int leftIndex = 0;
    int rightIndex = 0;
    while(leftIndex < leftP.size() && rightIndex < rightP.size()) {
      while(leftP.getPosition(leftIndex) < rightP.getPosition(rightIndex) -1) {
        if(++leftIndex >= leftP.size()) return new SimplePositionsList(hits);
      }
      while(rightP.getPosition(rightIndex) < leftP.getPosition(leftIndex) +1) {
        if(++rightIndex >= rightP.size()) return new SimplePositionsList(hits);
      }
      if(leftP.getPosition(leftIndex) +1 == rightP.getPosition(rightIndex)) {
        hits.add(leftP.getPosition(leftIndex));
        leftIndex++;
        rightIndex++;
      }
    }
    return new SimplePositionsList(hits);
  }

  public static int countUnordered(PositionsList left, PositionsList right, int width) {
    PositionsIterator iterA = left.getSpanIterator();
    PositionsIterator iterB = right.getSpanIterator();

    if(iterA.isDone() || iterB.isDone()) {
      return 0;
    }

    int count = 0;
    boolean hasNext = true;
    while(hasNext) {
      // choose minimum iterator based on start
      final PositionsIterator minIter = (iterA.currentBegin() < iterB.currentBegin()) ? iterA : iterB;
      final int minimumPosition = minIter.currentBegin();
      final int maximumPosition = Math.max(iterA.currentEnd(), iterB.currentEnd());

      // check for a match
      if(maximumPosition - minimumPosition <= width) {
        //extentCache.add(minimumPosition, maximumPosition);
        count++;
      }

      // move minimum iterator
      hasNext = minIter.next();
    }
    return count;
  }
}
