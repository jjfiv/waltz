package edu.umass.cs.ciir.waltz.coders.map.impl.vocab;

import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.collections.util.IterableFns;
import ciir.jfoley.chai.collections.util.ListFns;
import ciir.jfoley.chai.fn.TransformFn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * @author jfoley
 */
public interface ListIndex<T,K> {

  static <T,K> ListIndex<T,K> create(String kind, Iterable<T> inner, TransformFn<T, K> keyFn) {
    return create(kind, inner, keyFn, Comparing.defaultComparator());
  }
  static <T,K> ListIndex<T,K> create(String kind, Iterable<T> inner, TransformFn<T, K> keyFn, Comparator<K> keyCmp) {
    switch (kind.toLowerCase()) {
      case "none":
        return new NoIndex<>(inner, keyFn, keyCmp);
      case "hashall":
        return new HashAllIndex<>(inner, keyFn);
      default: throw new UnsupportedOperationException(kind);
    }
  }

  T find(K key);

  class NoIndex<T,K> implements ListIndex<T,K> {
    public final ArrayList<T> inner;
    public final TransformFn<T,K> keyFn;
    public final Comparator<K> keyCmp;

    public NoIndex(Iterable<T> inner, TransformFn<T, K> keyFn, Comparator<K> keyCmp) {
      this.inner = new ArrayList<>();
      IterableFns.intoSink(inner, this.inner::add);
      this.keyFn = keyFn;
      this.keyCmp = keyCmp;
    }

    @Override
    public T find(K key) {
      int pos = Collections.binarySearch(ListFns.lazyMap(inner, keyFn), key, keyCmp);
      if(pos < 0) return null;
      return inner.get(pos);
    }
  }

  class HashAllIndex<T,K> implements ListIndex<T,K> {
    private final HashMap<K,T> items;

    public HashAllIndex(Iterable<T> inner, TransformFn<T, K> keyFn) {
      items = new HashMap<>();
      for (T t : inner) {
        items.put(keyFn.transform(t), t);
      }
    }

    @Override
    public T find(K key) {
      return items.get(key);
    }
  }

}
