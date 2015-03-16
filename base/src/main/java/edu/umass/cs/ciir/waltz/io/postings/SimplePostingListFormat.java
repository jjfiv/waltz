package edu.umass.cs.ciir.waltz.io.postings;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.io.Coder;
import edu.umass.cs.ciir.waltz.io.coders.DeltaIntListCoder;
import edu.umass.cs.ciir.waltz.io.util.BufferList;
import edu.umass.cs.ciir.waltz.io.util.DataChunk;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class SimplePostingListFormat {

  public static class Builder<V> {
    /** The number of items to put in each block by default. */
    private final int blockSize;
    private final Coder<V> valCoder;
    private final Coder<List<Integer>> intsCoder;
    public List<PostingListChunk<V>> kvChunks;
    public PostingListChunk<V> currentChunk;

    public Builder(Coder<V> valCoder) {
      this(128, new DeltaIntListCoder(), valCoder);
    }
    public Builder(int blockSize, Coder<List<Integer>> intsCoder, Coder<V> valCoder) {
      this.blockSize = blockSize;
      this.valCoder = valCoder;
      this.intsCoder = intsCoder;
      kvChunks = new ArrayList<>();
      currentChunk = new PostingListChunk<>(intsCoder, valCoder);
    }

    public void add(int key, V value) {
      if(currentChunk.count() > blockSize) { flush(); }
      currentChunk.add(key, value);
    }

    private void flush() {
      kvChunks.add(currentChunk);
      currentChunk = new PostingListChunk<>(intsCoder, valCoder);
    }
  }

  public static class Reader<V> {
    public Reader(Coder<V> valCoder, InputStream stream) {

    }
  }

  public static class PostingListChunk<V> {
    public final IntList keys;
    public final List<V> vals;
    public final Coder<List<Integer>> intCoder;
    public final Coder<V> valCoder;

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
