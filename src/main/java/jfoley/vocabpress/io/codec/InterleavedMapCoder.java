package jfoley.vocabpress.io.codec;

import jfoley.vocabpress.io.Coder;
import jfoley.vocabpress.io.util.BufferList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Encode a map in traversal order: size, (KV)+
 * @author jfoley
 */
public class InterleavedMapCoder<K,V> extends Coder<Map<K,V>> {
  private final Coder<Integer> countCoder;
  private final Coder<K> keyCoder;
  private final Coder<V> valCoder;

  public InterleavedMapCoder(Coder<K> keyCoder, Coder<V> valCoder) {
    this(VByteCoders.ints, keyCoder, valCoder);
  }
  public InterleavedMapCoder(Coder<Integer> countCoder, Coder<K> keyCoder, Coder<V> valCoder) {
    assert(countCoder.knowsOwnSize());
    assert(keyCoder.knowsOwnSize());
    assert(valCoder.knowsOwnSize());
    this.countCoder = countCoder;
    this.keyCoder = keyCoder;
    this.valCoder = valCoder;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Override
  public ByteBuffer writeImpl(Map<K, V> obj) throws IOException {
    BufferList output = new BufferList();
    output.add(countCoder, obj.size());
    for (Map.Entry<K, V> kvEntry : obj.entrySet()) {
      output.add(keyCoder, kvEntry.getKey());
      output.add(valCoder, kvEntry.getValue());
    }
    return output.compact();
  }

  @Override
  public Map<K, V> readImpl(InputStream inputStream) throws IOException {
    int count = countCoder.read(inputStream);
    Map<K,V> output = new HashMap<>(count);
    for (int i = 0; i < count; i++) {
      K key = keyCoder.read(inputStream);
      V val = valCoder.read(inputStream);
      output.put(key, val);
    }
    return output;
  }
}
