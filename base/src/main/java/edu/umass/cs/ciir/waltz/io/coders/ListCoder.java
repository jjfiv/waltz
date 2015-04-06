package edu.umass.cs.ciir.waltz.io.coders;

import edu.umass.cs.ciir.waltz.io.Coder;
import edu.umass.cs.ciir.waltz.io.util.BufferList;

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
  public BufferList writeImpl(List<T> obj) throws IOException {
    int count = obj.size();
    BufferList bl = new BufferList();
    bl.add(countCoder.writeImpl(count));
    for (T t : obj) {
      bl.add(itemCoder, t);
    }
    return bl;
  }

  @Override
  public List<T> readImpl(InputStream inputStream) throws IOException {
    int amount = countCoder.read(inputStream);
    System.out.println(amount);
    List<T> output = new ArrayList<>(amount);
    for (int i = 0; i < amount; i++) {
      System.err.println(i);
      output.add(itemCoder.read(inputStream));
    }
    return output;
  }

  @Override
  public void write(OutputStream out, List<T> obj) throws IOException {
    int count = obj.size();
    System.out.println(count);
    countCoder.write(out, count);
    for (int i = 0; i < obj.size(); i++) {
      T t = obj.get(i);
      System.err.println(i);
      itemCoder.write(out, t);
    }
  }
}
