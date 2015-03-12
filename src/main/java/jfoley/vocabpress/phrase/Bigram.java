package jfoley.vocabpress.phrase;

import ciir.jfoley.chai.collections.list.IntList;
import jfoley.vocabpress.postings.positions.PositionsList;
import jfoley.vocabpress.postings.positions.SimplePositionsList;

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
}
