package edu.umass.cs.ciir.waltz.sys;

/**
 * @author jfoley
 */
public interface KeyMetadata<V, Subclass extends KeyMetadata<V, ?>> {
  int totalDocuments();

  void accumulate(Subclass m);

  void accumulate(int document, V item);

  Subclass zero();
}
