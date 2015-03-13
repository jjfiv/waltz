package jfoley.vocabpress.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author jfoley
 */
public abstract class Codec<T> {
  /** Reading from a ByteBuffer in memory. Default implementation piggy-backs on readImpl. */
  public T read(ByteBuffer buf) {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(buf.array(), buf.arrayOffset(), buf.limit())) {
      return readImpl(bais);
    } catch (IOException e) {
      throw new CodecException(e, this.getClass());
    }
  }
  public T read(InputStream is) {
    try {
      return readImpl(is);
    } catch (IOException e) {
      throw new CodecException(e, this.getClass());
    }
  }
  public ByteBuffer write(T input) {
    try {
      return writeImpl(input);
    } catch (IOException e) {
      throw new CodecException(e, this.getClass());
    }
  }

  /** Writing, abstract, may throw IOException. */
  public abstract ByteBuffer writeImpl(T obj) throws IOException;
  /** Reading, abstract, may throw IOException. */
  public abstract T readImpl(InputStream inputStream) throws IOException;
}
