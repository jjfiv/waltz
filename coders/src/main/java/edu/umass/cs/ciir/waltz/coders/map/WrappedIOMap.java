package edu.umass.cs.ciir.waltz.coders.map;

import ciir.jfoley.chai.collections.Pair;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley
 */
public abstract class WrappedIOMap<K,V> implements IOMap<K,V> {
  protected final IOMap<K,V> inner;

  public WrappedIOMap(IOMap<K, V> inner) {
    this.inner = inner;
  }

  @Override
  public long keyCount() {
    return inner.keyCount();
  }

  @Nonnull
  @Override
  public Map<String, Object> getConfig() {
    return inner.getConfig();
  }

  @Nullable
  @Override
  public V get(K key) throws IOException {
    return inner.get(key);
  }

  @Nullable
  @Override
  public StaticStream getSource(K key) throws IOException {
    return inner.getSource(key);
  }

  @Nonnull
  @Override
  public List<Pair<K, V>> getInBulk(List<K> keys) throws IOException {
    return inner.getInBulk(keys);
  }

  @Nonnull
  @Override
  public Iterable<K> keys() throws IOException {
    return inner.keys();
  }

  @Override
  public void close() throws IOException {
    inner.close();
  }
}
