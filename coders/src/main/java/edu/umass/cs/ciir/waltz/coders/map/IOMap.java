package edu.umass.cs.ciir.waltz.coders.map;

import ciir.jfoley.chai.collections.Pair;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley
 */
public interface IOMap<K, V> extends Closeable {
  long keyCount();
  @Nonnull Map<String, Object> getConfig();
  @Nullable V get(K key) throws IOException;
  @Nullable StaticStream getSource(K key) throws IOException;
  @Nonnull List<Pair<K,V>> getInBulk(List<K> keys) throws IOException;
  @Nonnull Iterable<K> keys() throws IOException;
}
