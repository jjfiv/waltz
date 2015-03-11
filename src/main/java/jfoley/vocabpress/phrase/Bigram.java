package jfoley.vocabpress.phrase;

import ciir.jfoley.chai.collections.list.IntList;

/**
 * @author jfoley
 */
public class Bigram {

  /**
   * Return the count of occurences of a bigram.
   * @param leftP the positions of the first term in the bigram.
   * @param rightP the positions of the second term in the bigram
   */
  public static int count(int[] leftP, int[] rightP) {
    int hits = 0;
    int leftIndex = 0;
    int rightIndex = 0;
    while(leftIndex < leftP.length && rightIndex < rightP.length) {
      while(leftP[leftIndex] < rightP[rightIndex]-1) {
        if(++leftIndex >= leftP.length) return hits;
      }
      while(rightP[rightIndex] < leftP[leftIndex]+1) {
        if(++rightIndex >= rightP.length) return hits;
      }
      if(leftP[leftIndex]+1 == rightP[rightIndex]) {
        hits++;
        leftIndex++;
        rightIndex++;
      }
    }
    return hits;
  }

  public static IntList positions(int[] leftP, int[] rightP) {
    IntList hits = new IntList(Math.min(leftP.length, rightP.length));
    int leftIndex = 0;
    int rightIndex = 0;
    while(leftIndex < leftP.length && rightIndex < rightP.length) {
      while(leftP[leftIndex] < rightP[rightIndex]-1) {
        if(++leftIndex >= leftP.length) return hits;
      }
      while(rightP[rightIndex] < leftP[leftIndex]+1) {
        if(++rightIndex >= rightP.length) return hits;
      }
      if(leftP[leftIndex]+1 == rightP[rightIndex]) {
        hits.add(leftP[leftIndex]);
        leftIndex++;
        rightIndex++;
      }
    }
    return hits;
  }
}
