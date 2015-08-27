package edu.umass.cs.ciir.waltz.dociter;

import ciir.jfoley.chai.collections.list.AChaiList;

/**
 * @author jfoley
 */
public class FastKeyBlock extends AChaiList<Integer> implements IKeyBlock {
  private final int[] keys;
  private final int size;
  private int minKey;
  private int maxKey;

  public FastKeyBlock(int[] keys, int size) {
    this.keys = keys;
    this.size = size;
    this.minKey = -1;
    this.maxKey = -1;
    if(size >= 1) {
      this.minKey = keys[0];
      this.maxKey = keys[size - 1];
    }
  }
  @Override public int minKey() { return minKey; }
  @Override public int maxKey() { return maxKey; }
  @Override public int size() { return size; }
  @Override public int getKey(int index) { return keys[index]; }
  @Override public Integer get(int index) { return getKey(index); }
}
