package jfoley.vocabpress.scoring.blockiter;

import java.util.AbstractList;
import java.util.List;

/**
 * @author jfoley
 */
public class KeyBlock extends AbstractList<Integer> {
  private final List<Integer> keys;

  protected KeyBlock(List<Integer> keys) {
    assert(keys.size() > 0);
    this.keys = keys;
  }
  public int min() {
    return keys.get(0);
  }

  public int max() {
    return keys.get(keys.size()-1);
  }

  public int getKey(int index) {
    return keys.get(index);
  }

  @Override
  public int size() {
    return keys.size();
  }

  @Override
  public Integer get(int index) {
    return getKey(index);
  }
}
