package jfoley.vocabpress.scoring.blockiter.op;

import jfoley.vocabpress.scoring.blockiter.IKeyBlock;
import jfoley.vocabpress.scoring.blockiter.KeyBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class KeyOperations {
  public static IKeyBlock intersection(IKeyBlock lhs, IKeyBlock rhs) {
    if(lhs.minKey() > rhs.maxKey() || rhs.minKey() > lhs.maxKey() || lhs.size() == 0 || rhs.size() == 0) {
      return IKeyBlock.EMPTY;
    }

    List<Integer> output = new ArrayList<>();

    int li = 0;
    int ri = 0;
    while(li < lhs.size() && ri < rhs.size()) {
      int lkey = lhs.getKey(li);
      int rkey = rhs.getKey(ri);
      if(lkey < rkey) {
        if(rkey > lhs.maxKey()) break;
        li++;
      } else if(lkey > rkey) {
        if(lkey > rhs.maxKey()) break;
        ri++;
      } else {
        output.add(lkey);
        li++; ri++;
      }
    }

    return new KeyBlock(output);
  }
}
