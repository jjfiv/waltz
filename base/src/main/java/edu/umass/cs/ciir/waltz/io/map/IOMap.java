package edu.umass.cs.ciir.waltz.io.map;

import ciir.jfoley.chai.collections.Pair;
import edu.umass.cs.ciir.waltz.io.streams.StaticStream;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley
 */
public interface IOMap<K, V> extends Closeable {
  public long keyCount();
  public Map<String, Object> getConfig();
  public V get(K key) throws IOException;
  public StaticStream getSource(K key) throws IOException;
  public List<Pair<K,V>> getInBulk(List<K> keys) throws IOException;
}
