package jfoley.vocabpress.postings;

/**
 * @author jfoley
 */
public interface Posting<X> extends Comparable<Posting<X>> {
  /** Returns the document id, referred to as a key here. */
  public int getKey();
  public X getValue();
}
