package edu.umass.cs.ciir.waltz.postings.extents;

/**
* @author jfoley
*/
public interface ExtentsIterator {
  /** returns true if there are no more elements to step over */
  public boolean isDone();
  /** returns !isDone() after moving once */
  public boolean next();
  /** Moves back to the beginning of the array. */
  public void reset();

  /** return the beginning of this extent */
  public int currentBegin();
  /** return the end of this extent -- if it's a position, this is always currentBegin()+1 */
  public int currentEnd();
}
