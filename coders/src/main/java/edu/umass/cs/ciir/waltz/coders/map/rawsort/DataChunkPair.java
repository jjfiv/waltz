package edu.umass.cs.ciir.waltz.coders.map.rawsort;

import ciir.jfoley.chai.collections.Pair;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteArray;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import javax.annotation.Nonnull;

/**
 * @author jfoley.
 */
public class DataChunkPair extends Pair<ByteArray, DataChunk> implements Comparable<DataChunkPair> {
  public DataChunkPair(DataChunk left, DataChunk right) {
    super(ByteArray.of(left), right);
  }

  /**
   * {@inheritDoc}
   * Only compare by keys.
   * @param o the other item.
   * @return a comparison integer.
   */
  @Override
  public int compareTo(@Nonnull DataChunkPair o) {
    return getKey().compareTo(o.getKey());
  }

  public <A,B> Pair<A,B> decode(Coder<A> keyCoder, Coder<B> valCoder) {
    return Pair.of(keyCoder.read(left), valCoder.read(right));
  }
}
