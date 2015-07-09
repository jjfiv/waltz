package edu.umass.cs.ciir.waltz.postings.docset;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
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
public class DeltaIntListMoverCoder extends Coder<Mover> {
  final Coder<Integer> itemCoder;
  final Coder<Integer> countCoder;

  public DeltaIntListMoverCoder() {
    this(VarUInt.instance, VarUInt.instance);
  }

  public DeltaIntListMoverCoder(Coder<Integer> itemCoder, Coder<Integer> countCoder) {
    this.itemCoder = itemCoder;
    this.countCoder = countCoder;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(Mover obj) throws IOException {
    BufferList bl = new BufferList();
    int total = obj.totalKeys();
    bl.add(countCoder, total);
    int prev = 0;

    for (; !obj.isDone(); obj.nextBlock()) {
      BufferList block = new BufferList();
      for (; !obj.isDoneWithBlock(); obj.nextKey()) {
        int x = obj.currentKey();
        int delta = x - prev;
        bl.add(itemCoder, delta);
        prev = x;
      }
      bl.add(block.compact());
    }
    return bl;
  }

  @Nonnull
  @Override
  public Mover read(StaticStream streamFn) throws IOException {
    return new DeltaIntListStreamMover(streamFn, itemCoder, countCoder);
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
