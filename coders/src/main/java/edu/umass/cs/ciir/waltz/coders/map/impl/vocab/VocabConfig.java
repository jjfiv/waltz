package edu.umass.cs.ciir.waltz.coders.map.impl.vocab;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;

import java.util.Comparator;

/**
 * @author jfoley
 */
public class VocabConfig<K> {
  public final Coder<K> keyCoder;
  public final Comparator<K> cmp;
  public final Coder<Long> offsetCoder;
  public final Coder<Integer> sizeCoder;

  public VocabConfig(Coder<K> keyCoder, Comparator<K> cmp) {
    this.keyCoder = keyCoder.lengthSafe();
    this.cmp = cmp;
    this.offsetCoder = FixedSize.longs;
    this.sizeCoder = VarUInt.instance;
  }
}
