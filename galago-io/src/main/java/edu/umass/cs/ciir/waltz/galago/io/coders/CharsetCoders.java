package edu.umass.cs.ciir.waltz.galago.io.coders;

import ciir.jfoley.chai.io.IO;
import ciir.jfoley.chai.lang.Module;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBufferDataChunk;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarInt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author jfoley
 */
public class CharsetCoders extends Module {
  public static final Charset charset = Charset.forName("UTF-8");

  public static final Coder<String> withoutLengthPrefix = new Coder<java.lang.String>() {
    @Override
    public DataChunk writeImpl(java.lang.String obj) throws IOException {
      return ByteBufferDataChunk.of(obj.getBytes(charset));
    }

    @Override
    public java.lang.String readImpl(InputStream inputStream) throws IOException {
      InputStreamReader reader = new InputStreamReader(inputStream, charset);
      return IO.readAll(reader);
    }

    @Override
    public java.lang.String read(ByteBuffer buf) {
      return new java.lang.String(buf.array(), buf.arrayOffset(), buf.limit(), charset);
    }

    @Override
    public boolean knowsOwnSize() {
      return false;
    }
  };

  public static final Coder<java.lang.String> withVByteLength = new LengthPrefixCoder<>(VarInt.instance, withoutLengthPrefix);
}
