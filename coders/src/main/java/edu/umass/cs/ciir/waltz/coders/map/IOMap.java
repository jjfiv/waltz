package edu.umass.cs.ciir.waltz.coders.map;

import ciir.jfoley.chai.collections.Pair;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley
 */
public interface IOMap<K, V> extends Closeable {
  long keyCount();
  Map<String, Object> getConfig();
  V get(K key) throws IOException;
  StaticStream getSource(K key) throws IOException;
  List<Pair<K,V>> getInBulk(List<K> keys) throws IOException;
  Iterable<K> keys() throws IOException;
}
