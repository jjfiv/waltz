package jfoley.vocabpress.io.codec;

import jfoley.vocabpress.io.Codec;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * @author jfoley
 */
public class UTF8 {
  public static final Charset charset = Charset.forName("UTF-8");

  public static final Codec<? extends CharSequence> general = new Codec<CharSequence>() {
    @Override
    public ByteBuffer write(CharSequence obj) {
      CharBuffer buff = CharBuffer.allocate(obj.length());
      buff.append(obj);
      return charset.encode(buff);
    }

    @Override
    public CharSequence read(ByteBuffer buf) {
      return charset.decode(buf);
    }
  };

  public static final Codec<String> strings = new Codec<String>() {
    @Override
    public ByteBuffer write(String obj) {
      return ByteBuffer.wrap(obj.getBytes(charset));
    }

    @Override
    public String read(ByteBuffer buf) {
      return new String(buf.array(), buf.arrayOffset(), buf.limit(), charset);
    }
  };
}
