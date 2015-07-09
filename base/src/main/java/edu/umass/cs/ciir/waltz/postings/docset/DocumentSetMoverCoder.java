package edu.umass.cs.ciir.waltz.postings.docset;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.streams.SkipInputStream;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;
import edu.umass.cs.ciir.waltz.dociter.movement.Mover;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class DocumentSetMoverCoder extends Coder<Mover> {
  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(Mover obj) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public Mover read(StaticStream streamFn) throws IOException {
    return new DeltaIntListStreamMover(streamFn, FixedSize.ints, VarUInt.instance);
  }

  @Nonnull
  @Override
  public Mover readImpl(InputStream inputStream) throws IOException {
    return read(new StaticStream() {
      @Override
      public SkipInputStream getNewStream() {
        return SkipInputStream.wrap(inputStream);
      }

      @Override
      public long length() {
        throw new UnsupportedOperationException();
      }
    });
  }
}
