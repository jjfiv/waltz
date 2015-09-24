package edu.umass.cs.ciir.waltz.postings.extents;

/**
* @author jfoley
*/
public interface SpanIterator {
  /** returns true if there are no more elements to step over */
  boolean isDone();
  /** returns !isDone() after moving once */
  boolean next();
  /** Moves back to the beginning of the array. */
  void reset();

  /** return the beginning of this extent */
  int currentBegin();
  /** return the end of this extent -- if it's a position, this is always currentBegin()+1 */
  int currentEnd();

  default String asString() {
    if(isDone()) {
      return "DONE";
    }
    //return "["+currentBegin()+","+currentEnd()+")";
    return Integer.toString(currentBegin());
  }
}
