package edu.umass.cs.ciir.waltz.coders.ints;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.coders.Coder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author jfoley
 */
public abstract class IntsCoder extends Coder<List<Integer>> {

  public abstract void readInto(IntList target, InputStream inputStream) throws IOException;

  @Nonnull
  @Override
  public IntList readImpl(InputStream inputStream) throws IOException {
    IntList data = new IntList();
    readInto(data, inputStream);
    return data;
  }

  @Nonnull
  public IntsCoder lengthSafe() {
    assert(this.knowsOwnSize());
    return this;
  }
}
