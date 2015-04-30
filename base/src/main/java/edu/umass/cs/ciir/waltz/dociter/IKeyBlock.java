package edu.umass.cs.ciir.waltz.dociter;

import java.util.Collections;
import java.util.List;

/**
 * @author jfoley
 */
public interface IKeyBlock extends List<Integer> {
  int minKey();
  int maxKey();
  int getKey(int index);
  @Override
  int size();

  IKeyBlock EMPTY = new KeyBlock(Collections.emptyList());
}
