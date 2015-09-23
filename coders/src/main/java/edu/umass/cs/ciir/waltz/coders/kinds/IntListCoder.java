package edu.umass.cs.ciir.waltz.coders.kinds;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteArray;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class IntListCoder extends Coder<IntList> {
  public static IntListCoder instance = new IntListCoder();

  private IntListCoder() { }

  @Nonnull
  @Override
  public Class<?> getTargetClass() {
    return IntList.class;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(IntList obj) throws IOException {
    return new ByteArray(obj.encode());
  }

  @Nonnull
  @Override
  public IntList readImpl(InputStream inputStream) throws IOException {
    return IntList.decode(inputStream);
  }
}
