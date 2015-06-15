package edu.umass.cs.ciir.waltz.io.postings.streaming;

import edu.umass.cs.ciir.waltz.coders.data.ByteArray;

import javax.annotation.Nonnull;

/**
 * @author jfoley
 */
public class ByteKeyPosting<V> implements Comparable<ByteKeyPosting<?>> {
  public final ByteArray key;
  public final int document;
  public final V value;

  public ByteKeyPosting(ByteArray key, int document, V value) {
    this.key = key;
    this.document = document;
    this.value = value;
  }

  @Override
  public int compareTo(@Nonnull ByteKeyPosting<?> o) {
    int cmp = key.compareTo(o.key);
    if (cmp != 0) return cmp;
    return Integer.compare(document, o.document);
  }
}
