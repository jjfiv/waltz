package jfoley.vocabpress.io.codec;

import ciir.jfoley.chai.io.IO;
import jfoley.vocabpress.io.Codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author jfoley
 */
public class UTF8 {
  public static final Charset charset = Charset.forName("UTF-8");

  public static final Codec<String> withoutLengthPrefix = new Codec<String>() {
    @Override
    public ByteBuffer writeImpl(String obj) throws IOException {
      return ByteBuffer.wrap(obj.getBytes(charset));
    }

    @Override
    public String readImpl(InputStream inputStream) throws IOException {
      InputStreamReader reader = new InputStreamReader(inputStream, charset);
      return IO.readAll(reader);
    }

    @Override
    public String read(ByteBuffer buf) {
      return new String(buf.array(), buf.arrayOffset(), buf.limit(), charset);
    }

    @Override
    public boolean knowsOwnSize() {
      return false;
    }
  };

  public static final Codec<String> withVByteLength = new LengthPrefixCodec<>(VByteCoders.ints, withoutLengthPrefix);
}
