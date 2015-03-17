package edu.umass.cs.ciir.waltz.io.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * @author jfoley
 */
public interface DataChunk extends Closeable {
  /** Not required for functionality, but useful for debugging. */
  int getByte(int index);

  /** Return the total number of bytes represented by this buffer. */
  long byteCount();
  /** Return the data inside this buffer as a ByteBuffer. */
  public ByteBuffer asByteBuffer();
  /** Return the data inside this as an InputStream. */
  public InputStream asInputStream();
  /** Write the data to the given OutputStream */
  public void write(OutputStream out) throws IOException;
  /** Write the data to the given WritableByteChannel */
  public void write(WritableByteChannel out) throws IOException;
}
