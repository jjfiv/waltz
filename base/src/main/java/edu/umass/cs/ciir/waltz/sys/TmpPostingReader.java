package edu.umass.cs.ciir.waltz.sys;

import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;

/**
 * @author jfoley
 */
public final class TmpPostingReader<K, M extends KeyMetadata<V, M>, V> implements Comparable<TmpPostingReader<K, M, V>> {
  final PostingsConfig<K, M, V> cfg;
  final InputStream input;
  final int keyCount;
  private final int totalDocuments;
  int keyIndex;
  K currentKey;
  M currentMetadata;
  int documentCount;
  int documentIndex;
  int currentDocument;
  V currentValue;

  public TmpPostingReader(PostingsConfig<K, M, V> cfg, InputStream input) {
    this.cfg = cfg;
    this.input = input;
    this.keyCount = VarUInt.instance.read(input);
    this.totalDocuments = VarUInt.instance.read(input);
    this.getNextKey();
  }

  public int getTotalDocuments() {
    return totalDocuments;
  }

  public M getCurrentMetadata() {
    return currentMetadata;
  }

  public boolean hasNextDocument() {
    return documentIndex < documentCount;
  }

  public boolean hasNextKey() {
    return keyIndex < keyCount;
  }

  public V getCurrentValue() { return currentValue; }

  @Nullable
  public K getNextKey() {
    while (hasNextDocument()) {
      readNext();
    }
    if (hasNextKey()) {
      currentKey = cfg.keyCoder.read(input);
      keyIndex++;
      documentIndex = 0;
      currentDocument = 0;
      currentMetadata = cfg.metadataCoder.read(input);
      documentCount = currentMetadata.totalDocuments();
      readNext();
      return currentKey;
    }
    currentKey = null;
    return null;
  }

  public void readNext() {
    assert (hasNextDocument());
    // read delta:
    currentDocument += VarUInt.instance.read(input);
    currentValue = cfg.valCoder.read(input);
    this.documentIndex++;
  }

  @Override
  public int compareTo(@Nonnull TmpPostingReader<K, M, V> o) {
    int cmp = cfg.keyCmp.compare(currentKey, o.currentKey);
    if (cmp != 0) return cmp;
    return Integer.compare(currentDocument, o.currentDocument);
  }
}
