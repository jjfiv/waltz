package jfoley.vocabpress.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author jfoley
 */
public abstract class Coder<T> {
  /** Reading from a byte[] in memory. */
  public T read(byte[] data) {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
      return readImpl(bais);
    } catch (IOException e) {
      throw new CodecException(e, this.getClass());
    }
  }
  /** Reading from a ByteBuffer in memory. */
  public T read(ByteBuffer buf) {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(buf.array(), buf.arrayOffset(), buf.limit())) {
      return readImpl(bais);
    } catch (IOException e) {
      throw new CodecException(e, this.getClass());
    }
  }

  /** Reading from an InputStream. */
  public T read(InputStream is) {
    try {
      return readImpl(is);
    } catch (IOException e) {
      throw new CodecException(e, this.getClass());
    }
  }

  /** Write to a new ByteBuffer in memory. */
  public ByteBuffer write(T input) {
    try {
      return writeImpl(input);
    } catch (IOException e) {
      throw new CodecException(e, this.getClass());
    }
  }

  /** If false, always wrap in a LengthPrefixCodec for the ability to read it when you're streaming things! */
  public abstract boolean knowsOwnSize();

  /** Writing, abstract, may throw IOException. */
  public abstract ByteBuffer writeImpl(T obj) throws IOException;
  /** Reading, abstract, may throw IOException. */
  public abstract T readImpl(InputStream inputStream) throws IOException;
}
