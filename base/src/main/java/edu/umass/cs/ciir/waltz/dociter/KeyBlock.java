package edu.umass.cs.ciir.waltz.dociter;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jfoley
 */
public class KeyBlock extends AbstractList<Integer> implements IKeyBlock {
  private final List<Integer> keys;
  private int minKey;
  private int maxKey;

  public KeyBlock(List<Integer> keys) {
    this.keys = keys;
    this.minKey = -1;
    this.maxKey = -1;
    if(keys.size() >= 1) {
      this.minKey = keys.get(0);
      this.maxKey = keys.get(keys.size() - 1);
    }
  }
  @Override public int minKey() { return minKey; }
  @Override public int maxKey() { return maxKey; }
  @Override public int size() { return keys.size(); }
  @Override public int getKey(int index) { return keys.get(index); }
  @Override public Integer get(int index) { return getKey(index); }

  public static KeyBlock of(Integer... data) {
    return new KeyBlock(Arrays.asList(data));
  }
}
