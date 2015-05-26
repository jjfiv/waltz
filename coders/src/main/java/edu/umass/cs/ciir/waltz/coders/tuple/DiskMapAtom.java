package edu.umass.cs.ciir.waltz.coders.tuple;

import ciir.jfoley.chai.collections.Pair;

import java.util.Comparator;
import java.util.Map;

/**
 * @author jfoley
 */
public class DiskMapAtom<K,V> extends Pair<K,V> {
  public DiskMapAtom(K left, V right) {
    super(left, right);
  }

  public Comparator<? super Map.Entry<K,V>> getComparator() {
    return Pair.cmpLeft();
  }


}
