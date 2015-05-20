package edu.umass.cs.ciir.waltz.galago.io.coders;

import ciir.jfoley.chai.lang.Module;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBufferDataChunk;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import org.lemurproject.galago.utility.compression.VByte;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class GalagoVByteCoders extends Module {
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
