package edu.umass.cs.ciir.waltz.io.coders;

import ciir.jfoley.chai.io.IO;
import edu.umass.cs.ciir.waltz.io.Coder;
import edu.umass.cs.ciir.waltz.io.util.ByteBufferDataChunk;
import edu.umass.cs.ciir.waltz.io.util.DataChunk;

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

  public static final Coder<String> withoutLengthPrefix = new Coder<String>() {
    @Override
    public DataChunk writeImpl(String obj) throws IOException {
      return ByteBufferDataChunk.of(obj.getBytes(charset));
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

  public static final Coder<String> withVByteLength = new LengthPrefixCoder<>(VByteCoders.ints, withoutLengthPrefix);
}
