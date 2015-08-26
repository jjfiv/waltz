package edu.umass.cs.ciir.waltz.io.postings;

import ciir.jfoley.chai.collections.list.AChaiList;
import ciir.jfoley.chai.collections.list.BitVector;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarInt;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsIterator;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class PositionsListCoder extends Coder<PositionsList> {
  public PositionsListCoder() {
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(PositionsList obj) throws IOException {
    ByteBuilder bl = new ByteBuilder();
    int count = obj.size();
    int prev = 0;
    bl.add(VarUInt.instance.write(count));

    for (int x : obj) {
      int delta = x - prev;
      bl.add(VarInt.instance, delta);
      prev = x;
    }

    return bl;
  }

  @Nonnull
  @Override
  public PositionsList readImpl(InputStream inputStream) throws IOException {
    BitVector bv = new BitVector(4000);
    int count = VarUInt.instance.readImpl(inputStream);
    int[] data = new int[count];

    int runningValue = 0;
    for (int i = 0; i < count; i++) {
      runningValue += VarInt.instance.readImpl(inputStream);
      data[i] = runningValue;
      bv.set(runningValue);
    }
    ArrayPosList ap = new ArrayPosList(data);
    ap.myBitVector = bv;
    return ap;
  }

  public final static class ArrayPosList extends AChaiList<Integer> implements PositionsList {
    final int[] data;
    public BitVector myBitVector = null;

    public ArrayPosList(int[] data) {
      this.data = data;
    }

    @Override
    public Integer get(int index) {
      return data[index];
    }

    @Override
    public int getPosition(int index) {
      return data[index];
    }

    @Override
    public int size() {
      return data.length;
    }

    @Override
    public PositionsIterator getSpanIterator() {
      return new PositionsIterator(this);
    }
  }

}
