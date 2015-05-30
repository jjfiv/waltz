package edu.umass.cs.ciir.waltz.coders.map;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * @author jfoley.
 */
public interface IOMapWriter<K extends Comparable<K>, V> extends Closeable, Flushable {
  void put(K key, V val) throws IOException;
  void putUnsafe(K key, DataChunk val) throws IOException;
  IOMapWriter<K, V> getSorting() throws IOException;
  @Override
  void close() throws IOException;

  Coder<K> getKeyCoder();
  Coder<V> getValueCoder();
}
