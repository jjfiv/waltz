package edu.umass.cs.ciir.waltz.sys;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley
 */
public interface PostingIndexWriter<K, V> extends Closeable {
  /**
   * Called once at the beginning of a write.
   * @param totalDocumentCount the number of documents to expect.
   */
  void writeHeader(int totalDocumentCount);

  /**
   * Start a new posting list for a given key.
   * @param key the key to write.
   * @param metadata the key's metadata.
   * @throws IOException
   */
  void writeNewKey(K key, KeyMetadata<V> metadata) throws IOException;

  void writePosting(int doc, V value) throws IOException;
}
