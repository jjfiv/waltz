package edu.umass.cs.ciir.waltz.sys;

/**
 * @author jfoley
 */
public interface KeyMetadata<V, Subclass extends KeyMetadata<V, ?>> {
  /**
   * Any type of metadata must at least know how many documents were seen.
   * @return the total number of documents.
   */
  int totalDocuments();

  /**
   * Add in the information as represented by the other metadata object.
   * @param m another metadata object of the same class.
   */
  void accumulate(Subclass m);

  /**
   * visit a document with a given value:
   * @param document the document id
   * @param item the value
   */
  void accumulate(int document, V item);

  /**
   * @return an instance of this metadata which is semantically equivalent to zero.
   */
  Subclass zero();
}
