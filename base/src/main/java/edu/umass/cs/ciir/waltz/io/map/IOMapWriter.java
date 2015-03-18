package edu.umass.cs.ciir.waltz.io.map;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley
 */
public interface IOMapWriter<K,V> extends Closeable {
  public void put(K key, V val) throws IOException;
}
