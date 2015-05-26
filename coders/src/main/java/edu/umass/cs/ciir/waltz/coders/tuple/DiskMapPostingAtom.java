package edu.umass.cs.ciir.waltz.coders.tuple;

/**
 * For ease of use when your keys are already Comparable.
 * @author jfoley
 */
public class DiskMapPostingAtom<K extends Comparable<K>,V> implements Comparable<DiskMapPostingAtom<K,V>> {
  private final K term;
  /** Right now, Waltz's scoring "movers" are limited to integer number of documents. */
  private final int document;
  private final V value;

  public DiskMapPostingAtom(K term, int document, V value) {
    this.term = term;
    this.document = document;
    this.value = value;
  }

  public K getTerm() { return term; }
  public int getDocument() { return document; }
  public V getValue() { return value; }

  @Override
  public int compareTo(DiskMapPostingAtom<K, V> o) {
    int cmp = term.compareTo(o.term);
    if(cmp != 0) return cmp;
    return Integer.compare(document, o.document);
  }
}
