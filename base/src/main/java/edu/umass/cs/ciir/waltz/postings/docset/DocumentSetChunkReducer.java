package edu.umass.cs.ciir.waltz.postings.docset;

import edu.umass.cs.ciir.waltz.coders.reduce.Reducer;

import java.util.Objects;

/**
 * @author jfoley
 */
public class DocumentSetChunkReducer<K> extends Reducer<DocumentSetChunk<K>> {
  @Override
  public boolean shouldMerge(DocumentSetChunk<K> lhs, DocumentSetChunk<K> rhs) {
    return lhs.docs.size() < 128 && Objects.equals(lhs.key, rhs.key);
  }

  @Override
  public DocumentSetChunk<K> merge(DocumentSetChunk<K> lhs, DocumentSetChunk<K> rhs) {
    lhs.docs.addAll(rhs.docs);
    return lhs;
  }
}
