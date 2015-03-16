package edu.umass.cs.ciir.waltz.io;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.io.coders.DeltaIntListCoder;
import edu.umass.cs.ciir.waltz.io.util.BufferList;
import edu.umass.cs.ciir.waltz.io.util.DataChunk;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class PostingListBuilder<V> {

  public static class PostingListChunk<V> {
    public final IntList keys;
    public final List<V> vals;
    public final Coder<List<Integer>> intCoder;
    public final Coder<V> valCoder;

    public PostingListChunk(Coder<V> valCoder) {
      this(new DeltaIntListCoder(), valCoder);
    }
    public PostingListChunk(Coder<List<Integer>> intCoder, Coder<V> valCoder) {
      this.intCoder = intCoder;
      this.valCoder = valCoder;
      keys = new IntList();
      vals = new ArrayList<>();
    }

    public void add(int key, V value) {
      keys.add(key);
      vals.add(value);
    }

    public int count() {
      assert(keys.size() == vals.size());
      return keys.size();
    }

    public DataChunk encode() {
      BufferList bl = new BufferList();
      bl.add(intCoder, keys);
      for (V val : vals) {
        bl.add(valCoder, val);
      }
      return bl;
    }
  }
}
