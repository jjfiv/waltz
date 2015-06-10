package edu.umass.cs.ciir.waltz.coders.kinds;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class FloatCoder extends Coder<Float> {
  Coder<Integer> bitsCoder = FixedSize.ints;

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(@Nonnull Float obj) throws IOException {
    return bitsCoder.writeImpl(Float.floatToRawIntBits(obj));
  }

  @Nonnull
  @Override
  public Float readImpl(InputStream inputStream) throws IOException {
    return Float.intBitsToFloat(FixedSize.ints.read(inputStream));
  }
}
