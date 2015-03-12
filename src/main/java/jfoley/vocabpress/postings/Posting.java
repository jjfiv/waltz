package jfoley.vocabpress.postings;

/**
 * @author jfoley
 */
public interface Posting extends Comparable<Posting> {
  /** Returns the document id, referred to as a key here. */
  public int getKey();
}
