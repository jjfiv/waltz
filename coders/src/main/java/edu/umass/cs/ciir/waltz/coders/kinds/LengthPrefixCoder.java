package edu.umass.cs.ciir.waltz.coders.kinds;

import ciir.jfoley.chai.IntMath;
import ciir.jfoley.chai.io.StreamFns;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author jfoley
 */
public class LengthPrefixCoder<T> extends Coder<T> {
  private final Coder<Integer> lengthCoder;
  private final Coder<T> payloadCoder;

  public LengthPrefixCoder(@Nonnull Coder<Integer> lengthCoder, @Nonnull Coder<T> payloadCoder) {
    assert lengthCoder.knowsOwnSize() : "Length prefix needs to be able to decode itself.";
    assert !payloadCoder.knowsOwnSize() : "Should only length-prefix things that need prefixing.";
    this.lengthCoder = lengthCoder;
    this.payloadCoder = payloadCoder;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public BufferList writeImpl(T obj) throws IOException {
    DataChunk payload = payloadCoder.writeImpl(obj);
    ByteBuffer length = lengthCoder.write(IntMath.fromLong(payload.byteCount()));
    BufferList bl = new BufferList();
    bl.add(length);
    bl.add(payload);
    return bl;
  }

  @Nonnull
  @Override
  public T readImpl(@Nonnull InputStream inputStream) throws IOException {
    int length = lengthCoder.readImpl(inputStream);
    byte[] data = StreamFns.readBytes(inputStream, length);
    return payloadCoder.read(ByteBuffer.wrap(data));
  }

  @Nonnull
  public static <V> Coder<V> wrap(@Nonnull Coder<V> inner) {
    if(inner.knowsOwnSize()) {
      return inner;
    } else {
      return new LengthPrefixCoder<>(VarUInt.instance, inner);
    }
  }
}
