package edu.umass.cs.ciir.waltz.sys;

import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public interface KeyMetadata<V> {
  /**
   * Any type of metadata must at least know how many documents were seen.
   * @return the total number of documents.
   */
  int totalDocuments();

  /**
   * @return the maximum document number encountered.
   */
  int maxDocument();

  /**
   * Add in the information as represented by the other metadata object.
   * @param m another metadata object of the same class.
   */
  void accumulate(KeyMetadata m);

  /**
   * visit a document with a given value:
   * @param document the document id
   * @param item the value
   */
  void accumulate(int document, V item);

  /**
   * @return an instance of this metadata which is semantically equivalent to zero.
   */
  KeyMetadata<V> zero();

  DataChunk encode();
  KeyMetadata<V> decode(InputStream input) throws IOException;
}
