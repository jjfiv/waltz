package edu.umass.cs.ciir.waltz.io.util;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author jfoley
 */
public interface DataChunk {
  /** Not required for functionality, but useful for debugging. */
  int getByte(int index);

  /** Return the total number of bytes represented by this buffer. */
  int byteCount();
  /** Return the data inside this buffer as a ByteBuffer. */
  public ByteBuffer asByteBuffer();
  /** Return the data inside this as an InputStream. */
  public InputStream asInputStream();
}
