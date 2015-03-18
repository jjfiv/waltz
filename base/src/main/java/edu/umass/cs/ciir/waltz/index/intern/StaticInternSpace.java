package edu.umass.cs.ciir.waltz.index.intern;

import java.util.Map;

/**
 * @author jfoley
 */
public interface StaticInternSpace<Val> {
  Iterable<Map.Entry<Val, Integer>> getAllItems();
  Val getValue(int query);
  int getId(Val query);
}
