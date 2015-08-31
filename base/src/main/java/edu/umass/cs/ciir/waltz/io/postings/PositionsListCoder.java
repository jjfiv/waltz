package edu.umass.cs.ciir.waltz.io.postings;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarInt;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class PositionsListCoder extends Coder<PositionsList> {
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

    for (int i = 0; i < count; i++) {
      int x = obj.getPosition(i);
      int delta = x - prev;
      bl.add(VarInt.instance, delta);
      prev = x;
    }

    return bl;
  }

  @Nonnull
  @Override
  public PositionsList readImpl(InputStream inputStream) throws IOException {
    int count = VarUInt.instance.readPrim(inputStream);
    int[] data = new int[count];

    int runningValue = 0;
    for (int i = 0; i < count; i++) {
      runningValue += VarInt.instance.readPrim(inputStream);
      data[i] = runningValue;
    }
    return new ArrayPosList(data, count);
  }

}
