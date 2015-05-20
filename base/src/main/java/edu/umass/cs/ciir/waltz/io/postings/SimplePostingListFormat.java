package edu.umass.cs.ciir.waltz.io.postings;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;
import edu.umass.cs.ciir.waltz.dociter.IKeyBlock;
import edu.umass.cs.ciir.waltz.dociter.IValueBlock;
import edu.umass.cs.ciir.waltz.dociter.KeyBlock;
import edu.umass.cs.ciir.waltz.dociter.ValueBlock;
import edu.umass.cs.ciir.waltz.dociter.movement.BlockPostingsMover;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.galago.io.coders.DeltaIntListCoder;
import edu.umass.cs.ciir.waltz.galago.io.coders.GalagoVByteCoders;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.MutableDataChunk;
import edu.umass.cs.ciir.waltz.coders.data.TmpFileDataChunk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class SimplePostingListFormat {

  public static class PostingCoder<V> extends PostingListCoder<V> {
    private final int blockSize;
    private final Coder<V> valCoder;
    private final Coder<List<Integer>> intsCoder;

    public PostingCoder(Coder<V> valCoder) throws IOException {
      this(128, new DeltaIntListCoder(), valCoder);
    }
    public PostingCoder(int blockSize, Coder<List<Integer>> intsCoder, Coder<V> valCoder) {
      this.blockSize = blockSize;
      assert(intsCoder.knowsOwnSize());
      assert(valCoder.knowsOwnSize());
      this.intsCoder = intsCoder;
      this.valCoder = valCoder;
    }

    @Override
    public DataChunk writeImpl(PostingMover<V> obj) throws IOException {
      Builder<V> writer = new Builder<>(blockSize, intsCoder, valCoder);
      for(; !obj.isDone(); obj.next()) {
        writer.add(obj.currentKey(), obj.getCurrentPosting());
      }
      return writer.getData();
    }

    @Override
    public PostingMover<V> read(StaticStream streamFn) throws IOException {
      return new BlockPostingsMover<>(new Reader<>(intsCoder, valCoder, streamFn));
    }
  }

  public static class Builder<V> {
    /** The number of items to put in each block by default. */
    private final int blockSize;
    public PostingListChunk<V> currentChunk;
    MutableDataChunk output;
    public int totalKeys = 0;

    public Builder(int blockSize, Coder<List<Integer>> intsCoder, Coder<V> valCoder) throws IOException {
      output = new TmpFileDataChunk();
      this.blockSize = blockSize;
      currentChunk = new PostingListChunk<>(intsCoder, valCoder);
    }

    public void add(int key, V value) throws IOException {
      if(currentChunk.count() >= blockSize) { writeCurrentBlock(); }
      currentChunk.add(key, value);
      totalKeys++;
    }

    private void writeCurrentBlock() throws IOException {
      DataChunk data = currentChunk.encode();
      assert(data.byteCount() < Integer.MAX_VALUE);
      output.add(GalagoVByteCoders.ints.write((int) data.byteCount()));
      output.add(data);
      currentChunk.clear();
    }

    public DataChunk getData() throws IOException {
      if(currentChunk.count() > 0) {
        writeCurrentBlock();
      }
      BufferList bl = new BufferList();
      bl.add(getMetadataChunk());
      bl.add(output);
      return bl;
    }

    public ByteBuffer getMetadataChunk() {
      // No matter what posting it is, it knows it's df.
      return GalagoVByteCoders.ints.write(totalKeys);
    }
  }

  public static class Reader<V> extends StaticStreamPostingsIterator<V> {
    private final Coder<List<Integer>> intsCoder;
    private final Coder<V> valCoder;
    private boolean haveReadCurrentValues;
    private long nextKeyBlockOffset;
    private int numKeysInThisBlock;
    private int totalKeys;
    private int usedKeys = 0;
    private boolean done;

    public Reader(Coder<List<Integer>> intsCoder, Coder<V> valCoder, StaticStream streamSource) {
      super(streamSource);
      this.intsCoder = intsCoder;
      this.valCoder = valCoder;
    }

    @Override
    public void readStreamHeader() throws IOException {
      numKeysInThisBlock = -1;
      haveReadCurrentValues = true;
      nextKeyBlockOffset = 0;
      done = false;
      totalKeys = GalagoVByteCoders.ints.read(stream);
      usedKeys = 0;
    }

    @Override
    public IKeyBlock nextKeyBlock() {
      if(done) return null;
      if(usedKeys == totalKeys) return null;

      try {
        // Skip values if possible.
        if (!haveReadCurrentValues) {
          stream.seek(nextKeyBlockOffset);
        }
        haveReadCurrentValues = false;
        // VByte: size of next block of keys+values
        int nextBlockSize = GalagoVByteCoders.ints.read(stream);
        nextKeyBlockOffset = stream.tell() + nextBlockSize;
        // Read in all the keys.
        List<Integer> keys = intsCoder.read(stream);
        this.numKeysInThisBlock = keys.size();
        usedKeys += numKeysInThisBlock;
        return new KeyBlock(keys);

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public IValueBlock<V> nextValueBlock() {
      if(done) return null;
      haveReadCurrentValues = true;

      List<V> values = new ArrayList<>(numKeysInThisBlock);
      for (int i = 0; i < numKeysInThisBlock; i++) {
        values.add(valCoder.read(stream));
      }

      return new ValueBlock<>(values);
    }

    @Override
    public int totalKeys() {
      return totalKeys;
    }
  }

  public static class PostingListChunk<V> {
    public final IntList keys;
    public final List<V> vals;
    public final Coder<List<Integer>> intsCoder;
    public final Coder<V> valCoder;

    public PostingListChunk(Coder<List<Integer>> intsCoder, Coder<V> valCoder) {
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

    public DataChunk encode() {
      BufferList bl = new BufferList();
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

}
