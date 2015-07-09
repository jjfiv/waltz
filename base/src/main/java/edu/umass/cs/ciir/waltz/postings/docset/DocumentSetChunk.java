package edu.umass.cs.ciir.waltz.postings.docset;

import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.collections.util.Comparing;

import javax.annotation.Nonnull;
import java.util.Comparator;

/**
 * @author jfoley
 */
public class DocumentSetChunk<K> implements Comparable<DocumentSetChunk<K>> {
  public K key;
  public IntList docs;
  Comparator<K> cmp;

  public DocumentSetChunk(K key, int doc) {
    this.key = key;
    this.docs = new IntList();
    this.docs.add(doc);
    this.cmp = Comparing.defaultComparator();
  }

  public DocumentSetChunk(K key, IntList docs) {
    this.key = key;
    this.docs = docs;
    this.cmp = Comparing.defaultComparator();
  }

  @Override
  public int compareTo(@Nonnull DocumentSetChunk<K> o) {
    int cv = cmp.compare(this.key, o.key);
    if (cv != 0) return cv;
    return Integer.compare(this.docs.get(0), o.docs.get(0));
  }
}
