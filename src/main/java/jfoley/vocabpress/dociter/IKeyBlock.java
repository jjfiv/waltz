package jfoley.vocabpress.dociter;

import java.util.Collections;
import java.util.List;

/**
 * @author jfoley
 */
public interface IKeyBlock extends List<Integer> {
  public int minKey();
  public int maxKey();
  public int getKey(int index);
  @Override
  public int size();

  public static final IKeyBlock EMPTY = new KeyBlock(Collections.emptyList());
}
