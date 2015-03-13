package jfoley.vocabpress.io.codec;

import jfoley.vocabpress.io.Codec;
import org.lemurproject.galago.utility.compression.VByte;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author jfoley
 */
public class VByteCoders {
  public static final Codec<Integer> ints = new Codec<Integer>() {
    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Override
    public ByteBuffer writeImpl(Integer obj) throws IOException {
      assert(obj != null);
      return ByteBuffer.wrap(VByte.compressInt(obj));
    }

    @Override
    public Integer readImpl(InputStream inputStream) throws IOException {
      return VByte.uncompressInt(new DataInputStream(inputStream));
    }
  };

  public static final Codec<Long> longs = new Codec<Long>() {
    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Override
    public ByteBuffer writeImpl(Long obj) throws IOException {
      assert(obj != null);
      return ByteBuffer.wrap(VByte.compressLong(obj));
    }

    @Override
    public Long readImpl(InputStream inputStream) throws IOException {
      return VByte.uncompressLong(new DataInputStream(inputStream));
    }
  };
}
