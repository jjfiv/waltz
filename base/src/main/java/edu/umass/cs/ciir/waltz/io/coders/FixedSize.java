package edu.umass.cs.ciir.waltz.io.coders;

import ciir.jfoley.chai.io.StreamFns;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.io.util.ByteBufferDataChunk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author jfoley
 */
public class FixedSize {
  public static final Coder<Integer> ints = new Coder<Integer>() {
    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Override
    public DataChunk writeImpl(Integer obj) throws IOException {
      ByteBuffer ofInt = ByteBuffer.allocate(4);
      ofInt.putInt(0, obj);
      return ByteBufferDataChunk.of(ofInt);
    }

    @Override
    public Integer readImpl(InputStream inputStream) throws IOException {
      return ByteBuffer.wrap(StreamFns.readBytes(inputStream, 4)).getInt();
    }
  };
  public static final Coder<Long> longs = new Coder<Long>() {
    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Override
    public DataChunk writeImpl(Long obj) throws IOException {
      ByteBuffer tmp = ByteBuffer.allocate(8);
      tmp.putLong(0, obj);
      return ByteBufferDataChunk.of(tmp);
    }

    @Override
    public Long readImpl(InputStream inputStream) throws IOException {
      return ByteBuffer.wrap(StreamFns.readBytes(inputStream, 8)).getLong();
    }
  };
}
