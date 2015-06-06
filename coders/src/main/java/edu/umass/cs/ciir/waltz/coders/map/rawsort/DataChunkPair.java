package edu.umass.cs.ciir.waltz.coders.map.rawsort;

import ciir.jfoley.chai.collections.Pair;
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

  @Override
  public int compareTo(@Nonnull DataChunkPair o) {
    return getKey().compareTo(o.getKey());
  }
}
