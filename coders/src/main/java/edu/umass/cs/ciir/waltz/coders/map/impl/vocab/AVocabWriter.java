package edu.umass.cs.ciir.waltz.coders.map.impl.vocab;

import ciir.jfoley.chai.IntMath;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author jfoley
 */
public abstract class AVocabWriter<K> implements VocabWriter<K> {
  protected K key = null;
  protected long start;

  @Override
  public void onKey(@Nonnull K key, long start) throws IOException {
    assert (this.key == null);
    this.key = key;
    this.start = start;
  }

  @Override
  public void onFinishKey(long end) throws IOException {
    assert (key != null);
    K prevKey = key;
    key = null;
    onEntry(prevKey, start, IntMath.fromLong(end - start));
  }
}
