package jfoley.vocabpress.io;

import java.nio.ByteBuffer;

/**
 * @author jfoley
 */
public interface Codec<T> {
  public ByteBuffer write(T obj);
  public T read(ByteBuffer buf);
}
