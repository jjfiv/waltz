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
    int read = is.read(buf);
    if(read < -1) {
      throw new EOFException();
    }
    if(read < amt) {
      throw new IOException();
    }
    return buf;
  }

  public static InputStream fromByteBuffer(ByteBuffer compact) {
    // TODO, add a ByteBufferInputStream implementation from a trusted source: e.g. http://stackoverflow.com/questions/4332264/wrapping-a-bytebuffer-with-an-inputstream
    return new ByteArrayInputStream(compact.array());
  }
}
