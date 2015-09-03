package edu.umass.cs.ciir.waltz.coders.map.impl.vocab;

import ciir.jfoley.chai.collections.util.Comparing;
import edu.umass.cs.ciir.waltz.coders.files.FileSlice;

import javax.annotation.Nonnull;
import java.util.Comparator;

/**
 * @author jfoley
 */
public class VocabEntry<K> implements Comparable<VocabEntry<K>> {
  public final K key;
  public final long offset;
  public final int size;
  private final Comparator<K> cmp;

  public VocabEntry(K key, long offset, int size) {
    this(key, offset, size, Comparing.defaultComparator());
  }

  public VocabEntry(K key, long offset, int size, Comparator<K> keyCmp) {
    this.key = key;
    this.offset = offset;
    this.size = size;
    this.cmp = keyCmp;
  }

  @Override
  public String toString() {
    return "<" + key + ":" + offset + "+" + size + ">";
  }

  @Override
  public int compareTo(@Nonnull VocabEntry<K> o) {
    return cmp.compare(key, o.key);
  }

  public FileSlice slice() {
    return new FileSlice(offset, offset+size);
  }
}
