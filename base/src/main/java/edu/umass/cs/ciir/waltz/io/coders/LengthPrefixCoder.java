package edu.umass.cs.ciir.waltz.io.coders;

import ciir.jfoley.chai.io.StreamFns;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author jfoley
 */
public class LengthPrefixCoder<T> extends Coder<T> {
  private final Coder<Integer> lengthCoder;
  private final Coder<T> payloadCoder;

  public LengthPrefixCoder(Coder<Integer> lengthCoder, Coder<T> payloadCoder) {
    assert lengthCoder.knowsOwnSize() : "Length prefix needs to be able to decode itself.";
    assert !payloadCoder.knowsOwnSize() : "Should only length-prefix things that need prefixing.";
    this.lengthCoder = lengthCoder;
    this.payloadCoder = payloadCoder;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Override
  public BufferList writeImpl(T obj) throws IOException {
    DataChunk payload = payloadCoder.writeImpl(obj);
    assert(payload.byteCount() <= Integer.MAX_VALUE);
    ByteBuffer length = lengthCoder.write((int) payload.byteCount());
    BufferList bl = new BufferList();
    bl.add(length);
    bl.add(payload);
    return bl;
  }

  @Override
  public T readImpl(InputStream inputStream) throws IOException {
    int length = lengthCoder.readImpl(inputStream);
    byte[] data = StreamFns.readBytes(inputStream, length);
    return payloadCoder.read(ByteBuffer.wrap(data));
  }
}
