package jfoley.vocabpress.io.codec;

import jfoley.vocabpress.io.Codec;
import jfoley.vocabpress.io.util.BufferList;
import jfoley.vocabpress.io.util.StreamFns;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author jfoley
 */
public class LengthPrefixCodec<T> extends Codec<T> {
  private final Codec<Integer> lengthCodec;
  private final Codec<T> payloadCodec;

  public LengthPrefixCodec(Codec<Integer> lengthCodec, Codec<T> payloadCodec) {
    assert lengthCodec.knowsOwnSize() : "Length prefix needs to be able to decode itself.";
    assert !payloadCodec.knowsOwnSize() : "Should only length-prefix things that need prefixing.";
    this.lengthCodec = lengthCodec;
    this.payloadCodec = payloadCodec;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Override
  public ByteBuffer writeImpl(T obj) throws IOException {
    ByteBuffer payload = payloadCodec.writeImpl(obj);
    ByteBuffer length = lengthCodec.write(payload.limit());
    BufferList bl = new BufferList();
    bl.add(length);
    bl.add(payload);
    return bl.compact();
  }

  @Override
  public T readImpl(InputStream inputStream) throws IOException {
    int length = lengthCodec.readImpl(inputStream);
    byte[] data = StreamFns.readBytes(inputStream, length);
    return payloadCodec.read(ByteBuffer.wrap(data));
  }
}
