package edu.umass.cs.ciir.waltz.index.intern;

import java.util.*;

/**
 * @author jfoley
 */
public class SortedInternSpace<Val extends Comparable<? super Val>> implements StaticInternSpace<Val> {
  private final ArrayList<Val> values;

  public SortedInternSpace() {
    values = new ArrayList<>();
  }
  public SortedInternSpace(List<Val> items) {
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
