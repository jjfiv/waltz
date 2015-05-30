package edu.umass.cs.ciir.waltz.coders.map;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley.
 */
public interface IOMapWriter<K extends Comparable<K>, V> extends Closeable {
  void put(K key, V val) throws IOException;
  IOMapWriter<K, V> getSorting() throws IOException;
  @Override
  void close() throws IOException;
}
