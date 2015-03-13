package jfoley.vocabpress.io.coders;

import jfoley.vocabpress.io.Coder;
import jfoley.vocabpress.io.util.BufferList;
import jfoley.vocabpress.io.util.StreamFns;

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
  public ByteBuffer writeImpl(T obj) throws IOException {
    ByteBuffer payload = payloadCoder.writeImpl(obj);
    ByteBuffer length = lengthCoder.write(payload.limit());
    BufferList bl = new BufferList();
    bl.add(length);
    bl.add(payload);
    return bl.compact();
  }

  @Override
  public T readImpl(InputStream inputStream) throws IOException {
    int length = lengthCoder.readImpl(inputStream);
    byte[] data = StreamFns.readBytes(inputStream, length);
    return payloadCoder.read(ByteBuffer.wrap(data));
  }
}
