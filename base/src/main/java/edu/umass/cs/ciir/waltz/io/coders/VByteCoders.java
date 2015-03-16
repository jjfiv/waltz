package edu.umass.cs.ciir.waltz.io.coders;

import edu.umass.cs.ciir.waltz.io.Coder;
import edu.umass.cs.ciir.waltz.io.util.ByteBufferDataChunk;
import edu.umass.cs.ciir.waltz.io.util.DataChunk;
import org.lemurproject.galago.utility.compression.VByte;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class VByteCoders {
  public static final Coder<Integer> ints = new Coder<Integer>() {
    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Override
    public DataChunk writeImpl(Integer obj) throws IOException {
      assert(obj != null);
      return ByteBufferDataChunk.of(VByte.compressInt(obj));
    }

    @Override
    public Integer readImpl(InputStream inputStream) throws IOException {
      return VByte.uncompressInt(new DataInputStream(inputStream));
    }
  };

  public static final Coder<Long> longs = new Coder<Long>() {
    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Override
    public DataChunk writeImpl(Long obj) throws IOException {
      assert(obj != null);
      return ByteBufferDataChunk.of(VByte.compressLong(obj));
    }

    @Override
    public Long readImpl(InputStream inputStream) throws IOException {
      return VByte.uncompressLong(new DataInputStream(inputStream));
    }
  };
}
