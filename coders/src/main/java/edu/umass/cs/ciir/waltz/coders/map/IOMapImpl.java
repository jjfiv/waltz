package edu.umass.cs.ciir.waltz.coders.map;

import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.util.IterableFns;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.CoderException;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley
 */
public class IOMapImpl<K,V> implements IOMap<K,V> {
  private final RawIOMap inner;
  private final Coder<K> keyCoder;
  private final Coder<V> valCoder;

  public IOMapImpl(RawIOMap inner, Coder<K> keyCoder, Coder<V> valCoder) {
    this.inner = inner;
    this.keyCoder = keyCoder;
    this.valCoder = valCoder;
  }

  @Override
  public long keyCount() {
    return inner.keyCount();
  }

  @Nonnull
  @Override
  public Map<String, Object> getConfig() {
    return inner.getConfig();
  }

  @Override
  public V get(K key) throws IOException {
    StaticStream result = inner.get(keyCoder.writeData(key));
    if(result == null) return null;
    return valCoder.read(result);
  }

  @Nullable
  @Override
  public StaticStream getSource(K key) throws IOException {
    return inner.get(keyCoder.writeData(key));
  }

  @Nonnull
  @Override
  public List<Pair<K, V>> getInBulk(List<K> keys) throws IOException {
    List<DataChunk> encodedKeys = new ArrayList<>(keys.size());
    for (K key : keys) {
      encodedKeys.add(keyCoder.writeData(key));
    }
    List<Pair<K, V>> output = new ArrayList<>(keys.size());
    for (Pair<DataChunk, StaticStream> kv : inner.getInBulk(encodedKeys)) {
      K key = null;
      V val = null;
      try {
        StaticStream valueStream = kv.getValue();
        key = keyCoder.read(kv.getKey());
        val = valCoder.read(valueStream);
        output.add(Pair.of(key,val));
      } catch (CoderException|EOFException ex) {
        // Use try catch to return as many results as possible:
        System.err.println("Couldn't decode either key or value: "+Pair.of(key,val));
        throw ex;
      }
    }
    return output;
  }

  @Nonnull
  @Override
  public Iterable<K> keys() throws IOException {
    return IterableFns.map(inner.keys(), keyCoder::read);
  }

  @Nonnull
  @Override
  public Iterable<Pair<K, V>> items() throws IOException {
    return getInBulk(IterableFns.intoList(keys()));
  }

  @Override
  public void close() throws IOException {
    inner.close();
  }
}
