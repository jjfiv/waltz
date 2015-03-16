package jfoley.vocabpress.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley
 */
public interface IOMap<K, V> extends Closeable {
  long keyCount();

  public Map<String, Object> getConfig();
  public V get(K key) throws IOException;
  public InputStream getStream(K key) throws IOException;
  public Map<K,V> getInBulk(List<K> keys) throws IOException;
}
