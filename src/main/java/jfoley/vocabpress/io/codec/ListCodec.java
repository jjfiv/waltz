package jfoley.vocabpress.io.codec;

import jfoley.vocabpress.io.Codec;
import jfoley.vocabpress.io.util.BufferList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class ListCodec<T> extends Codec<List<T>> {

  private final Codec<Integer> countCodec;
  private final Codec<T> itemCodec;

  public ListCodec(Codec<T> itemCodec) {
    this(VByteCoders.ints, itemCodec);
  }
  public ListCodec(Codec<Integer> countCodec, Codec<T> itemCodec) {
    this.countCodec = countCodec;
    assert(itemCodec.knowsOwnSize());
    this.itemCodec = itemCodec;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Override
  public ByteBuffer writeImpl(List<T> obj) throws IOException {
    int count = obj.size();
    BufferList bl = new BufferList();
    bl.add(countCodec.writeImpl(count));
    for (T t : obj) {
      bl.add(itemCodec, t);
    }
    return bl.compact();
  }

  @Override
  public List<T> readImpl(InputStream inputStream) throws IOException {
    int amount = countCodec.read(inputStream);
    List<T> output = new ArrayList<>(amount);
    for (int i = 0; i < amount; i++) {
      output.add(itemCodec.read(inputStream));
    }
    return output;
  }
}
