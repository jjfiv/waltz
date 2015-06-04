package edu.umass.cs.ciir.waltz.postings;

import javax.annotation.Nonnull;

/**
 * @author jfoley
 */
public class SimplePosting<V> implements Posting<V> {
  protected final int document;
  protected final V value;

  public SimplePosting(int document, V value) {
    this.document = document;
    this.value = value;
  }

  @Override
  public int getKey() {
    return document;
  }

  @Nonnull
  @Override
  public V getValue() {
    return value;
  }

  @Override
  public int compareTo(@Nonnull Posting<V> o) {
    return Integer.compare(document, o.getKey());
  }

  public static SimplePosting<Integer> Count(int doc, int count) {
    return new SimplePosting<>(doc, count);
  }
}
