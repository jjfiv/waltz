package edu.umass.cs.ciir.waltz.coders.kinds;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class ListCoder<T> extends Coder<List<T>> {

  private final Coder<Integer> countCoder;
  private final Coder<T> itemCoder;

  public ListCoder(Coder<T> itemCoder) {
    this(VarUInt.instance, itemCoder);
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

  @Nonnull
  @Override
  public BufferList writeImpl(List<T> obj) throws IOException {
    int count = obj.size();
    BufferList bl = new BufferList();
    bl.add(countCoder.writeImpl(count));
    for (T t : obj) {
      bl.add(itemCoder, t);
    }
    return bl;
  }

  @Nonnull
  @Override
  public List<T> readImpl(InputStream inputStream) throws IOException {
    int amount = countCoder.read(inputStream);
    List<T> output = new ArrayList<>(amount);
    for (int i = 0; i < amount; i++) {
      output.add(itemCoder.read(inputStream));
    }
    return output;
  }

  @Override
  public void write(OutputStream out, List<T> obj) {
    int count = obj.size();
    countCoder.write(out, count);
    for (T t : obj) {
      itemCoder.write(out, t);
    }
  }
}
