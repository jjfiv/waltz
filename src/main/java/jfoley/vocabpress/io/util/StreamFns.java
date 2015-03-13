package jfoley.vocabpress.io.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

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
}
