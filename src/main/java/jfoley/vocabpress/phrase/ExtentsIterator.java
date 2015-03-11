package jfoley.vocabpress.phrase;

/**
* @author jfoley
*/
public interface ExtentsIterator {
  /** returns true if there are no more elements to step over */
  public boolean isDone();
  /** returns !isDone() after moving once */
  public boolean next();
  /** return the beginning of this extent */
  public int currentBegin();
  /** return the end of this extent -- if it's a position, this is always currentBegin()+1 */
  public int currentEnd();
}
