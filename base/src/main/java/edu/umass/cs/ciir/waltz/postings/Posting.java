package edu.umass.cs.ciir.waltz.postings;

import javax.annotation.Nonnull;

/**
 * @author jfoley
 */
public interface Posting<X> extends Comparable<Posting<X>> {
  /** Returns the document id, referred to as a key here. */
  int getKey();
  @Nonnull X getValue();
}
