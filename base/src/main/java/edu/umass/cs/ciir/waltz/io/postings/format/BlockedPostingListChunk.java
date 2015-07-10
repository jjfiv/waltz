package edu.umass.cs.ciir.waltz.io.postings.format;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import java.util.ArrayList;
import java.util.List;

/**
 * A builder that collects a keyed set of values for writing to a file.
 * @param <V>
 */
public class BlockedPostingListChunk<V> {
  public final IntList keys;
  public final List<V> vals;
  public final Coder<List<Integer>> intsCoder;
  public final Coder<V> valCoder;

  public BlockedPostingListChunk(Coder<List<Integer>> intsCoder, Coder<V> valCoder) {
    this.intsCoder = intsCoder;
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

  /**
   * Encode this data as a chunk for writing to a file.
   * @return the data in this chunk, keys first then values.
   */
  public DataChunk encode() {
    ByteBuilder bl = new ByteBuilder();
    bl.add(intsCoder, keys);
    for (V val : vals) {
      bl.add(valCoder, val);
    }
    return bl;
  }

  public void clear() {
    keys.clear();
    vals.clear();
  }
}
