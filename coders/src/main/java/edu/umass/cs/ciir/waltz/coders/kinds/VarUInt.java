package edu.umass.cs.ciir.waltz.coders.kinds;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteArray;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * This is based on Galago's implementation.
 * @author jfoley
 */
public class VarUInt extends Coder<Integer> {
  public static final VarUInt instance = new VarUInt();

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  // Magic bits for this format:
  private static int DONE_BIT =  0b10000000;
  private static int REG_DATA =  0b01111111;

  @Nonnull
  @Override
  public DataChunk writeImpl(@Nonnegative Integer obj) throws IOException {
    assert(obj != null);
    int x = obj;
    assert(x >= 0);

    byte[] data = new byte[6];
    int put = 0;
    while((x != 0) || (put == 0)) {
      if(x < DONE_BIT) { // fits in 7 bits:
        data[put++] = (byte) (x | DONE_BIT);
        break;
      } else {
        data[put++] = (byte) (x & REG_DATA);
        x >>>= 7;
      }
    }

    assert((data[put-1] & DONE_BIT) != 0);
    assert(put >= 1 && put <= 6);
    return new ByteArray(Arrays.copyOf(data, put));
  }

  /*
  public void write(OutputStream out, @Nonnegative Integer obj) {
    try {
      assert (obj != null);
      int x = obj;
      assert (x >= 0);

      int put = 0;
      while ((x != 0) || (put == 0)) {
        if (x < DONE_BIT) { // fits in 7 bits:
          out.write((byte) (x | DONE_BIT));
          put++;
          break;
        } else {
          out.write((byte) (x & REG_DATA));
          put++;
          x >>>= 7;
        }
      }

      assert (put >= 1 && put <= 6);
    } catch (IOException e) {
      throw new CoderException(e, this.getClass());
    }
  }
  */

  @Nonnegative
  @Nonnull
  @Override
  public Integer readImpl(InputStream inputStream) throws IOException {
    int result = 0;

    for (int position = 0; true; position++) {
      assert position < 6;
      int x= inputStream.read();
      if (x == -1) throw new EOFException();
      int b = x & 0xff;
      if ((b & DONE_BIT) != 0) {
        result |= ((b & REG_DATA) << (position * 7));
        break;
      } else {
        result |= (b << (position * 7));
      }
    }

    assert(result >= 0);
    return result;
  }
}
