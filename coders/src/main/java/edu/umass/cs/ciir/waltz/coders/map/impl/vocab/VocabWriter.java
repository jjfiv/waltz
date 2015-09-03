package edu.umass.cs.ciir.waltz.coders.map.impl.vocab;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley
 */
public interface VocabWriter<K> extends Closeable {
  void onKey(@Nonnull K key, long start) throws IOException;

  void onFinishKey(long end) throws IOException;

  void onEntry(@Nonnull K key, long start, int size) throws IOException;

  void writeHeader() throws IOException;
}
