package edu.umass.cs.ciir.waltz.coders.kinds;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This integer list provides delta-gapped writing and reading, for sorted lists of integers.
 * @author jfoley
 */
public class DeltaIntListCoder extends Coder<List<Integer>> {
  private final Coder<Integer> countCoder;
  private final Coder<Integer> itemCoder;

  public DeltaIntListCoder() {
    this(VarUInt.instance, VarInt.instance);
  }
  public DeltaIntListCoder(Coder<Integer> countCoder, Coder<Integer> itemCoder) {
    this.countCoder = countCoder;
    this.itemCoder = itemCoder;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(List<Integer> obj) throws IOException {
    ByteBuilder bl = new ByteBuilder();
    int count = obj.size();
    int prev = 0;
    bl.add(countCoder.write(count));

    for (int x : obj) {
      int delta = x - prev;
      bl.add(itemCoder, delta);
      prev = x;
    }

    return bl;
  }

  @Nonnull
  @Override
  public List<Integer> readImpl(InputStream inputStream) throws IOException {
    int amount = countCoder.read(inputStream);
    IntList output = new IntList();
    int delta = 0;
    for (int i = 0; i < amount; i++) {
      delta += itemCoder.read(inputStream);
      output.add(delta);
    }
    return output;
  }

}
