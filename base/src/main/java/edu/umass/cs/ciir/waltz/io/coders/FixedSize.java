package edu.umass.cs.ciir.waltz.io.coders;

import edu.umass.cs.ciir.waltz.io.Coder;
import edu.umass.cs.ciir.waltz.io.util.BufferList;
import edu.umass.cs.ciir.waltz.io.util.StreamFns;

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
    public BufferList writeImpl(Integer obj) throws IOException {
      ByteBuffer ofInt = ByteBuffer.allocate(4);
      ofInt.putInt(0, obj);
      return BufferList.singleton(ofInt);
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
    public BufferList writeImpl(Long obj) throws IOException {
      ByteBuffer tmp = ByteBuffer.allocate(8);
      tmp.putLong(0, obj);
      return BufferList.singleton(tmp);
    }

    @Override
    public Long readImpl(InputStream inputStream) throws IOException {
      return ByteBuffer.wrap(StreamFns.readBytes(inputStream, 8)).getLong();
    }
  };
}
