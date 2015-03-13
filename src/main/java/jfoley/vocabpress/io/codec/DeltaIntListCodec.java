package jfoley.vocabpress.io.codec;

import ciir.jfoley.chai.collections.list.IntList;
import jfoley.vocabpress.io.Codec;
import jfoley.vocabpress.io.util.BufferList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * This integer list provides delta-gapped writing and reading, for sorted lists of integers.
 * @author jfoley
 */
public class DeltaIntListCodec extends Codec<List<Integer>> {
  private final Codec<Integer> countCodec;
  private final Codec<Integer> itemCodec;

  public DeltaIntListCodec() {
    this(VByteCoders.ints, VByteCoders.ints);
  }
  public DeltaIntListCodec(Codec<Integer> countCodec, Codec<Integer> itemCodec) {
    this.countCodec = countCodec;
    this.itemCodec = itemCodec;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Override
  public ByteBuffer writeImpl(List<Integer> obj) throws IOException {
    BufferList bl = new BufferList();
    int count = obj.size();
    int prev = 0;
    bl.add(countCodec.write(count));

    for (int x : obj) {
      int delta = x - prev;
      assert(delta > 0);
      bl.add(itemCodec, delta);
      prev = x;
    }

    return bl.compact();
  }

  @Override
  public List<Integer> readImpl(InputStream inputStream) throws IOException {
    int amount = countCodec.read(inputStream);
    IntList output = new IntList();
    int delta = 0;
    for (int i = 0; i < amount; i++) {
      delta += itemCodec.read(inputStream);
      output.add(delta);
    }
    return output;
  }
}
