package edu.umass.cs.ciir.waltz.coders.kinds;

import ciir.jfoley.chai.fn.TransformFn;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class MappingCoder<T, INNER> extends Coder<T> {
  private final Coder<INNER> baseCoder;
  private final TransformFn<T, INNER> wrapFn;
  private final TransformFn<INNER, T> unwrapFn;

  public MappingCoder(Coder<INNER> baseCoder, TransformFn<T,INNER> wrapFn, TransformFn<INNER,T> unwrapFn) {
    this.baseCoder = baseCoder;
    this.wrapFn = wrapFn;
    this.unwrapFn = unwrapFn;
  }

  @Override
  public boolean knowsOwnSize() {
    return baseCoder.knowsOwnSize();
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(T obj) throws IOException {
    return baseCoder.writeImpl(wrapFn.transform(obj));
  }

  @Nonnull
  @Override
  public T readImpl(InputStream inputStream) throws IOException {
    return unwrapFn.transform(baseCoder.readImpl(inputStream));
  }
}
