package edu.umass.cs.ciir.waltz.coders.map.impl.vocab;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;

import java.util.Comparator;

/**
 * @author jfoley
 */
public class VocabConfig<K> {
  public final Coder<K> keyCoder;
  public final Comparator<K> cmp;
  public final Coder<Long> offsetCoder;
  public final Coder<Long> sizeCoder;

  public VocabConfig(Coder<K> keyCoder, Comparator<K> cmp) {
    this.keyCoder = keyCoder;
    this.cmp = cmp;
    this.offsetCoder = FixedSize.longs;
    this.sizeCoder = FixedSize.longs;
  }
}
