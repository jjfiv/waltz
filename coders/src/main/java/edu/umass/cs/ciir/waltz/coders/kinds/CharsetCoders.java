package edu.umass.cs.ciir.waltz.coders.kinds;

import ciir.jfoley.chai.io.IO;
import ciir.jfoley.chai.lang.Module;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBufferDataChunk;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import javax.annotation.Nonnull;
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

  /**
   * This is a UTF-8 string coder. If you want a length-prefixed version, call .lengthSafe() on it.
   */
  public static final Coder<String> utf8 = new Coder<String>() {
    @Nonnull
    @Override
    public DataChunk writeImpl(String obj) throws IOException {
      return ByteBufferDataChunk.of(obj.getBytes(charset));
    }

    @Nonnull
    @Override
    public String readImpl(InputStream inputStream) throws IOException {
      InputStreamReader reader = new InputStreamReader(inputStream, charset);
      return IO.readAll(reader);
    }

    @Nonnull
    @Override
    public String read(ByteBuffer buf) {
      return new String(buf.array(), buf.arrayOffset(), buf.limit(), charset);
    }

    @Override
    public boolean knowsOwnSize() {
      return false;
    }
  };
}
