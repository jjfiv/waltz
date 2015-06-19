package edu.umass.cs.ciir.waltz.coders.kinds;

import ciir.jfoley.chai.collections.ArrayListMap;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author jfoley
 */
public class MapCoder<K,V> extends Coder<Map<K,V>> {
  final Coder<K> keyCoder;
  final Coder<V> valCoder;

  public MapCoder(Coder<K> keyCoder, Coder<V> valCoder) {
    this.keyCoder = keyCoder.lengthSafe();
    this.valCoder = valCoder.lengthSafe();
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(Map<K, V> obj) throws IOException {
    BufferList output = new BufferList();
    output.add(VarUInt.instance, obj.size());
    for (Map.Entry<K, V> kvEntry : obj.entrySet()) {
      output.add(keyCoder, kvEntry.getKey());
      output.add(valCoder, kvEntry.getValue());
    }
    return output.compact();
  }

  @Nonnull
  @Override
  public Map<K, V> readImpl(InputStream inputStream) throws IOException {
    int count = VarUInt.instance.readImpl(inputStream);
    ArrayListMap<K,V> data = new ArrayListMap<>(count);
    for (int i = 0; i < count; i++) {
      K key = keyCoder.readImpl(inputStream);
      V val = valCoder.readImpl(inputStream);
      data.put(key, val);
    }
    return data;
  }
}
