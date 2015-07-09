package edu.umass.cs.ciir.waltz.coders.map.impl;

import edu.umass.cs.ciir.waltz.coders.files.FileSlice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;

/**
 * Galago called this part the "vocab" so I will too.
 * @param <K> the type of the keys.
 */
public interface WaltzDiskMapVocabReader<K> extends Closeable {
  @Nullable
  FileSlice find(K key) throws IOException;
  @Nonnull
  Iterable<K> keys() throws IOException;

  long count();
}
