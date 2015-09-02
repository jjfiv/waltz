package edu.umass.cs.ciir.waltz.sys;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley
 */
public interface PostingIndexWriter<K, V> extends Closeable {
  void writeNewKey(K key) throws IOException;

  void writeMetadata(KeyMetadata<V> metadata) throws IOException;

  void writePosting(int doc, V value) throws IOException;

  void setDocumentCount(int totalDocumentCount);
}
