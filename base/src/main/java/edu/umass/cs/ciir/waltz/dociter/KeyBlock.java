package edu.umass.cs.ciir.waltz.dociter;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jfoley
 */
public class KeyBlock extends AbstractList<Integer> implements IKeyBlock {
  private final List<Integer> keys;

  public KeyBlock(List<Integer> keys) {
    this.keys = keys;
  }
  @Override
  public int minKey() {
    return keys.get(0);
  }

  @Override
  public int maxKey() {
    return keys.get(keys.size()-1);
  }

  @Override
  public int getKey(int index) {
    return keys.get(index);
  }

  @Override
  public Integer get(int index) {
    return getKey(index);
  }

  @Override
  public int size() {
    return keys.size();
  }

  public static KeyBlock of(Integer... data) {
    return new KeyBlock(Arrays.asList(data));
  }
}
