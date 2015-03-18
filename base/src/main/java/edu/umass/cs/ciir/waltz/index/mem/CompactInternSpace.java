package edu.umass.cs.ciir.waltz.index.mem;

import java.util.*;

/**
 * TODO, find bugs in here.
 * @author jfoley
 */
public class CompactInternSpace<Val extends Comparable<? super Val>> implements InternSpace<Val> {
  private final ArrayList<Val> values;

  public CompactInternSpace() {
    values = new ArrayList<>();
  }
  public CompactInternSpace(List<Val> items) {
    values = new ArrayList<>(items);
  }

  @Override
  public int getId(Val query) {
    return Collections.binarySearch(values, query);
  }

  @Override
  public Val getValue(int query) {
    return values.get(query);
  }

  @Override
  public void put(int first, Val second) {
    values.ensureCapacity(first+1);
    while(values.size() <= first) {
      values.add(null);
    }
    values.set(first, second);
  }

  @Override
  public int insertOrGet(Val k) {
    int index = getId(k);
    if(index < 0) {
      index = values.size();
      values.add(k);
    }
    return index;
  }

  @Override
  public Iterable<Map.Entry<Val, Integer>> getAllItems() {
    return new AbstractList<Map.Entry<Val, Integer>>() {
      @Override
      public Map.Entry<Val, Integer> get(int index) {
        return new AbstractMap.SimpleImmutableEntry<>(values.get(index), index);
      }

      @Override
      public int size() {
        return values.size();
      }
    };
  }
}
