package edu.umass.cs.ciir.waltz.io.util;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author jfoley
 */
public class StreamFns {
  public static byte[] readBytes(InputStream is, int amt) throws IOException {
    byte[] buf = new byte[amt];

    // Begin I/O loop:
    int off = 0;
    while(true) {
      int read = is.read(buf, off, amt);
      if (read < -1) {
        throw new EOFException();
      }
      if(read == amt) break;

      // Ugh; try again
      off += read;
      amt -= read;
    }
    return buf;
  }

  public static InputStream fromByteBuffer(ByteBuffer compact) {
    // TODO, add a ByteBufferInputStream implementation from a trusted source: e.g. http://stackoverflow.com/questions/4332264/wrapping-a-bytebuffer-with-an-inputstream
    return new ByteArrayInputStream(compact.array());
  }

  public static byte[] readAll(ByteBuffer buffer) throws IOException {
    InputStream str = fromByteBuffer(buffer);
    return readBytes(str, buffer.limit());
  }
}
