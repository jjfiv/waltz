package edu.umass.cs.ciir.waltz.galago.io.coders;

import ciir.jfoley.chai.lang.Module;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBufferDataChunk;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import org.lemurproject.galago.utility.compression.VByte;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class GalagoVByteCoders extends Module {
  public static final Coder<Integer> ints = new Coder<Integer>() {
    @Nonnull
    @Override
    public Class<?> getTargetClass() {
      return Integer.class;
    }

    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Nonnull
    @Override
    public DataChunk writeImpl(Integer obj) throws IOException {
      assert(obj != null);
      return ByteBufferDataChunk.of(VByte.compressInt(obj));
    }

    @Nonnull
    @Override
    public Integer readImpl(InputStream inputStream) throws IOException {
      return VByte.uncompressInt(new DataInputStream(inputStream));
    }
  };

  public static final Coder<Long> longs = new Coder<Long>() {
    @Nonnull
    @Override
    public Class<?> getTargetClass() {
      return Long.class;
    }

    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Nonnull
    @Override
    public DataChunk writeImpl(Long obj) throws IOException {
      assert(obj != null);
      return ByteBufferDataChunk.of(VByte.compressLong(obj));
    }

    @Nonnull
    @Override
    public Long readImpl(InputStream inputStream) throws IOException {
      return VByte.uncompressLong(new DataInputStream(inputStream));
    }
  };
}
