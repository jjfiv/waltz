package edu.umass.cs.ciir.waltz.coders.map.impl.vocab;

import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.collections.util.ListFns;
import ciir.jfoley.chai.fn.TransformFn;
import ciir.jfoley.chai.random.ReservoirSampler;

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
      case "threelevel":
        return new ThreeLevelIndex<>(inner, keyFn, keyCmp);
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

  class ThreeLevelIndex<T,K> implements ListIndex<T,K> {
    public final int LinearSize = 32;
    public final int TopMostLevel = 1024;
    public final ArrayList<K> secondLevel;
    public final ArrayList<T> inner;
    public final TransformFn<T,K> keyFn;
    public final Comparator<K> keyCmp;
    public final ArrayList<K> thirdLevel;
    public final IntList thirdLevelPtrs;

    public ThreeLevelIndex(Iterable<T> inner, TransformFn<T, K> keyFn, Comparator<K> keyCmp) {
      this.secondLevel = new ArrayList<>();
      this.inner = new ArrayList<>();
      Random rand = new Random(13);
      ReservoirSampler<Pair<K, Integer>> secondLevelSampled = new ReservoirSampler<>(rand, TopMostLevel);
      for (T t : inner) {
        int index = this.inner.size();
        if(index % LinearSize == 0) {
          int sindex = secondLevel.size();
          K skey = keyFn.transform(t);
          secondLevel.add(skey);
          secondLevelSampled.add(Pair.of(skey, sindex));
        }
        this.inner.add(t);
      }

      ArrayList<Pair<K,Integer>> pairs = new ArrayList<>(secondLevelSampled);
      Collections.sort(pairs, (lhs, rhs) -> keyCmp.compare(lhs.left, rhs.left));

      thirdLevel = new ArrayList<>(pairs.size()+1);
      thirdLevelPtrs = new IntList(pairs.size()+1);

      thirdLevel.add(keyFn.transform(this.inner.get(0)));
      thirdLevelPtrs.add(0);

      for (Pair<K, Integer> pair : pairs) {
        thirdLevel.add(pair.left);
        thirdLevelPtrs.add(pair.right);
      }
      thirdLevelPtrs.add(secondLevel.size());
      thirdLevel.add(keyFn.transform(ListFns.getLast(this.inner)));


      this.keyFn = keyFn;
      this.keyCmp = keyCmp;
    }

    public T findDeep(K key, int start) {
      return findDeep(key, start, start + LinearSize);
    }
    public T findDeep(K key, int start, int end) {
      int remaining = this.inner.size() - start;
      int step = Math.min(end - start, remaining);
      for (int i = 0; i < step; i++) {
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
      List<K> block = ListFns.lazyMap(ListFns.slice(inner, start, start + LinearSize), keyFn);
      int pos = Collections.binarySearch(block, key, keyCmp);
      if(pos < 0) return null;
      return inner.get(start+pos);
    }

    public T find2(K key, int start, int end) {
      for (int i = start; i < end; i++) {
        K candidate = secondLevel.get(i);
        int cmp = keyCmp.compare(key, candidate);
        if(cmp == 0) {
          return inner.get(i*LinearSize);
        } else if(cmp < 0) {
          return findDeep(key, (i-1) * LinearSize);
        }
      }
      return findDeep(key, (end-1)* LinearSize);
      /*int off = Collections.binarySearch(secondLevel, key, keyCmp);
      if(off >= 0) {
        return inner.get(off*LinearSize);
      }
      int which = (((off + 1) * -1) -1) * LinearSize;
      return findDeep(key, which);*/
    }

    @Override
    public T find(K key) {
      int off = Collections.binarySearch(thirdLevel, key, keyCmp);
      if(off > TopMostLevel) {
        return findDeep(key, (secondLevel.size()-1) * LinearSize, inner.size());
      }
      if(off >= 0) {
        int secondIndex = thirdLevelPtrs.get(off);
        return inner.get(secondIndex*LinearSize);
      }
      int whichThird = (off + 1) * -1 -1;
      if(whichThird > TopMostLevel) {
        return findDeep(key, (secondLevel.size()-1) * LinearSize, inner.size());
      }
      int startSecond = thirdLevelPtrs.get(whichThird);
      int endSecond = thirdLevelPtrs.get(whichThird+1);
      return find2(key, startSecond, endSecond);
    }
  }


}
