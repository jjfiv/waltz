package edu.umass.cs.ciir.waltz.galago.io;

import edu.umass.cs.ciir.waltz.coders.streams.SkipInputStream;
import org.lemurproject.galago.utility.buffer.CachedBufferDataStream;

import java.io.IOException;

/**
 * @author jfoley
 */
public class GalagoSkipInputStream extends SkipInputStream {
  private final CachedBufferDataStream inner;

  public GalagoSkipInputStream(CachedBufferDataStream inner) {
    this.inner = inner;
  }

  @Override
  public int read() throws IOException {
    return inner.read();
  }

  @Override
  public int read(byte[] b) throws IOException  {
    return inner.read(b);
  }

  @Override
  public int read(byte[] b, int offset, int amount) throws IOException  {
    return inner.read(b, offset, amount);
  }

  @Override
  public long tell() throws IOException {
    return inner.getPosition();
  }

  /** Moves to the given offset into this input stream. */
  @Override
  public void seek(long offset) throws IOException {
    if(offset >= inner.length()) {
      inner.seek(offset - 1);
      return;
    }
    inner.seek(offset);
  }
  @Override
  public void seekRelative(long delta) throws IOException {
    assert(delta > 0);
    inner.seek(tell() + delta);
  }

  @Override
  public void close() throws IOException {
    inner.close();
  }
}
