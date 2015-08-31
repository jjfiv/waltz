package edu.umass.cs.ciir.waltz.sys.tmp;

import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.sys.KeyMetadata;
import edu.umass.cs.ciir.waltz.sys.PostingsConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public final class TmpPostingReader<K, V> implements Comparable<TmpPostingReader<K, V>> {
  final PostingsConfig<K, V> cfg;
  final InputStream input;
  final int keyCount;
  private final int totalDocuments;
  int keyIndex;
  K currentKey;
  KeyMetadata<V> currentMetadata;
  int documentCount;
  int documentIndex;
  int currentDocument;
  V currentValue;

  public TmpPostingReader(PostingsConfig<K, V> cfg, InputStream input) {
    this.cfg = cfg;
    this.input = input;
    this.keyCount = VarUInt.instance.read(input);
    this.totalDocuments = VarUInt.instance.read(input);
    this.getNextKey();
  }

  public int getTotalDocuments() {
    return totalDocuments;
  }

  @Nonnull
  public KeyMetadata<V> getCurrentMetadata() {
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
      try {
        currentMetadata = cfg.metadata.decode(input);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
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
  public int compareTo(@Nonnull TmpPostingReader<K, V> o) {
    int cmp = cfg.keyCmp.compare(currentKey, o.currentKey);
    if (cmp != 0) return cmp;
    return Integer.compare(currentDocument, o.currentDocument);
  }
}
