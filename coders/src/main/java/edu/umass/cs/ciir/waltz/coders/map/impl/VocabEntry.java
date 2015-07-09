package edu.umass.cs.ciir.waltz.coders.map.impl;

import ciir.jfoley.chai.collections.util.Comparing;

import javax.annotation.Nonnull;
import java.util.Comparator;

/**
 * @author jfoley
 */
public class VocabEntry<K> implements Comparable<VocabEntry<K>> {
  public final K key;
  public final long offset;
  private final Comparator<K> cmp;

  public VocabEntry(K key, long offset) {
    this(key, offset, Comparing.defaultComparator());
  }

  public VocabEntry(K key, long offset, Comparator<K> keyCmp) {
    this.key = key;
    this.offset = offset;
    this.cmp = keyCmp;
  }

  @Override
  public String toString() {
    return "<" + key + ":" + offset + ">";
  }

  @Override
  public int compareTo(@Nonnull VocabEntry<K> o) {
    return cmp.compare(key, o.key);
  }
}
