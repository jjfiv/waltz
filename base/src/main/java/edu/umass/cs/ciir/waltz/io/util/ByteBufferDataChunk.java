package edu.umass.cs.ciir.waltz.io.util;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author jfoley
 */
public class ByteBufferDataChunk implements DataChunk {
  private final ByteBuffer buffer;
  private ByteBufferDataChunk(ByteBuffer buf) {
    this.buffer = buf;
  }

  @Override
  public int getByte(int index) {
    return buffer.get(index) & 0xff;
  }

  @Override
  public int byteCount() {
    return buffer.limit();
  }

  @Override
  public ByteBuffer asByteBuffer() {
    return buffer;
  }

  @Override
  public InputStream asInputStream() {
    return StreamFns.fromByteBuffer(buffer);
  }

  public static ByteBufferDataChunk of(ByteBuffer buf) {
    return new ByteBufferDataChunk(buf);
  }
  public static ByteBufferDataChunk of(byte[] data) {
    return new ByteBufferDataChunk(ByteBuffer.wrap(data));
  }
}
