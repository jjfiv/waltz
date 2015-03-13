package jfoley.vocabpress.io.coders;

import jfoley.vocabpress.io.Coder;
import jfoley.vocabpress.io.util.BufferList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class ListCoder<T> extends Coder<List<T>> {

  private final Coder<Integer> countCoder;
  private final Coder<T> itemCoder;

  public ListCoder(Coder<T> itemCoder) {
    this(VByteCoders.ints, itemCoder);
  }
  public ListCoder(Coder<Integer> countCoder, Coder<T> itemCoder) {
    assert(countCoder.knowsOwnSize());
    assert(itemCoder.knowsOwnSize());
    this.countCoder = countCoder;
    this.itemCoder = itemCoder;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Override
  public ByteBuffer writeImpl(List<T> obj) throws IOException {
    int count = obj.size();
    BufferList bl = new BufferList();
    bl.add(countCoder.writeImpl(count));
    for (T t : obj) {
      bl.add(itemCoder, t);
    }
    return bl.compact();
  }

  @Override
  public List<T> readImpl(InputStream inputStream) throws IOException {
    int amount = countCoder.read(inputStream);
    List<T> output = new ArrayList<>(amount);
    for (int i = 0; i < amount; i++) {
      output.add(itemCoder.read(inputStream));
    }
    return output;
  }
}
