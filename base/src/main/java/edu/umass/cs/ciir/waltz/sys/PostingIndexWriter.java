package edu.umass.cs.ciir.waltz.sys;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley
 */
public interface PostingIndexWriter<K, M, V> extends Closeable {
  void writeNewKey(K key) throws IOException;

  void writeMetadata(M metadata) throws IOException;

  void writePosting(int doc, V value) throws IOException;
}
