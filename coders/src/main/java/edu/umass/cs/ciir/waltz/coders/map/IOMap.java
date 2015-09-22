package edu.umass.cs.ciir.waltz.coders.map;

import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.util.IterableFns;
import com.github.benmanes.caffeine.cache.CacheLoader;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley
 */
public interface IOMap<K, V> extends Closeable, CacheLoader<K,V> {
  long keyCount();
  @Nonnull Map<String, Object> getConfig();
  @Nullable V get(K key) throws IOException;
  @Nullable StaticStream getSource(K key) throws IOException;
  @Nonnull List<Pair<K,V>> getInBulk(List<K> keys) throws IOException;
  @Nonnull Iterable<K> keys() throws IOException;
  @Nonnull Iterable<Pair<K,V>> items() throws IOException;

  @Override
  default V load(@Nonnull K key) {
    try {
      return get(key);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  default Map<K, V> loadAll(@Nonnull Iterable<? extends K> keys) {
    try {
      HashMap<K,V> found = new HashMap<>();
      for (Pair<K, V> kv : getInBulk(IterableFns.intoList(keys))) {
        found.put(kv.left, kv.right);
      }
      return found;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
