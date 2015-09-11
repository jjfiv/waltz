package edu.umass.cs.ciir.waltz.coders.map.impl.vocab;

import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.collections.util.ListFns;
import ciir.jfoley.chai.fn.TransformFn;

import java.util.*;

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
      case "twolevel":
        return new TwoLevelIndex<>(inner, keyFn, keyCmp);
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
      for (T t : inner) {
        this.inner.add(t);
      }
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

  class TwoLevelIndex<T,K> implements ListIndex<T,K> {
    public final int LinearSize = 32;
    public final ArrayList<K> firstLevel;
    public final ArrayList<T> inner;
    public final TransformFn<T,K> keyFn;
    public final Comparator<K> keyCmp;

    public TwoLevelIndex(Iterable<T> inner, TransformFn<T, K> keyFn, Comparator<K> keyCmp) {
      this.firstLevel = new ArrayList<>();
      this.inner = new ArrayList<>();
      for (T t : inner) {
        int index = this.inner.size();
        if(index % LinearSize == 0) {
          firstLevel.add(keyFn.transform(t));
        }
        this.inner.add(t);
      }
      this.keyFn = keyFn;
      this.keyCmp = keyCmp;
    }

    public T findDeep(K key, int start) {
      int remaining = this.inner.size() - start;
      for (int i = 0; i < Math.min(remaining, LinearSize); i++) {
        T current = inner.get(start + i);
        K candidate = keyFn.transform(current);
        int cmp = keyCmp.compare(key, candidate);
        if(cmp == 0) {
          return current;
        } else if(cmp < 0) break;
      }
      return null;
    }

    // slower for LinearSize=32
    public T findDeepBS(K key, int start) {
      List<K> block = ListFns.lazyMap(ListFns.slice(inner, start, start+ LinearSize), keyFn);
      int pos = Collections.binarySearch(block, key, keyCmp);
      if(pos < 0) return null;
      return inner.get(start+pos);
    }

    @Override
    public T find(K key) {
      int off = Collections.binarySearch(firstLevel, key, keyCmp);
      if(off >= 0) {
        return inner.get(off*LinearSize);
      }
      int which = (((off + 1) * -1) -1) * LinearSize;
      return findDeep(key, which);
    }
  }

}
