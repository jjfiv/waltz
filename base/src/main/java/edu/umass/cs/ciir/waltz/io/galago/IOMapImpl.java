package edu.umass.cs.ciir.waltz.io.galago;

import ciir.jfoley.chai.collections.Pair;
import edu.umass.cs.ciir.waltz.io.Coder;
import edu.umass.cs.ciir.waltz.io.map.IOMap;
import edu.umass.cs.ciir.waltz.io.map.RawIOMap;
import edu.umass.cs.ciir.waltz.io.streams.StaticStream;
import edu.umass.cs.ciir.waltz.io.util.DataChunk;

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

  @Override
  public StaticStream getSource(K key) throws IOException {
    return inner.get(keyCoder.writeData(key));
  }

  @Override
  public List<Pair<K, V>> getInBulk(List<K> keys) throws IOException {
    List<DataChunk> encodedKeys = new ArrayList<>(keys.size());
    for (K key : keys) {
      encodedKeys.add(keyCoder.writeData(key));
    }
    List<Pair<K, V>> output = new ArrayList<>(keys.size());
    for (Pair<DataChunk, StaticStream> kv : inner.getInBulk(encodedKeys)) {
      output.add(Pair.of(keyCoder.read(kv.getKey()), valCoder.read(kv.getValue())));
    }
    return output;
  }

  @Override
  public void close() throws IOException {

  }
}
