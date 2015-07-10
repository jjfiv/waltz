package edu.umass.cs.ciir.waltz.coders.kinds;

import ciir.jfoley.chai.collections.Pair;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * This writes pairs to files.
 * @author jfoley
 */
public class PairCoder<K, V> extends Coder<Pair<K, V>> {
  private final Coder<K> lhsCoder;
  private final Coder<V> rhsCoder;

  public PairCoder(Coder<K> lhsCoder, Coder<V> rhsCoder) {
    assert(lhsCoder.knowsOwnSize());
    assert(rhsCoder.knowsOwnSize());

    this.lhsCoder = lhsCoder;
    this.rhsCoder = rhsCoder;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(Pair<K, V> obj) throws IOException {
    ByteBuilder output = new ByteBuilder();
    output.add(lhsCoder, obj.getKey());
    output.add(rhsCoder, obj.getValue());
    return output;
  }

  @Nonnull
  @Override
  public Pair<K, V> readImpl(InputStream inputStream) throws IOException {
    K key = lhsCoder.readImpl(inputStream);
    V value = rhsCoder.readImpl(inputStream);
    return Pair.of(key, value);
  }
}
