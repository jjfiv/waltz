package edu.umass.cs.ciir.waltz.io.postings;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.dociter.IKeyBlock;
import edu.umass.cs.ciir.waltz.dociter.IValueBlock;
import edu.umass.cs.ciir.waltz.dociter.KeyBlock;
import edu.umass.cs.ciir.waltz.dociter.ValueBlock;
import edu.umass.cs.ciir.waltz.io.CodecException;
import edu.umass.cs.ciir.waltz.io.Coder;
import edu.umass.cs.ciir.waltz.io.coders.DeltaIntListCoder;
import edu.umass.cs.ciir.waltz.io.coders.VByteCoders;
import edu.umass.cs.ciir.waltz.io.streams.StaticStream;
import edu.umass.cs.ciir.waltz.io.util.BufferList;
import edu.umass.cs.ciir.waltz.io.util.DataChunk;
import edu.umass.cs.ciir.waltz.io.util.TmpFileDataChunk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class SimplePostingListFormat {

  public static class Writer<V> {
    /** The number of items to put in each block by default. */
    private final int blockSize;
    public PostingListChunk<V> currentChunk;
    TmpFileDataChunk output;
    public int totalKeys = 0;

    public Writer(Coder<V> valCoder) throws IOException {
      this(128, new DeltaIntListCoder(), valCoder);
    }
    public Writer(int blockSize, Coder<List<Integer>> intsCoder, Coder<V> valCoder) throws IOException {
      output = new TmpFileDataChunk();
      this.blockSize = blockSize;
      currentChunk = new PostingListChunk<>(intsCoder, valCoder);
    }

    public void add(int key, V value) throws IOException {
      if(currentChunk.count() > blockSize) { writeCurrentBlock(); }
      currentChunk.add(key, value);
      totalKeys++;
    }

    private void writeCurrentBlock() throws IOException {
      DataChunk data = currentChunk.encode();
      assert(data.byteCount() < Integer.MAX_VALUE);
      output.add(VByteCoders.ints.write((int) data.byteCount()));
      output.add(data);
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
      return VByteCoders.ints.write(totalKeys);
    }
  }

  public static class Reader<V> extends StaticStreamPostingsIterator<V> {
    private final Coder<List<Integer>> intsCoder;
    private final Coder<V> valCoder;
    private boolean haveReadCurrentValues;
    private long nextKeyBlockOffset;
    private int numKeysInThisBlock;
    private boolean done;

    public Reader(Coder<List<Integer>> intsCoder, Coder<V> valCoder, StaticStream streamSource) {
      super(streamSource);
      this.intsCoder = intsCoder;
      this.valCoder = valCoder;
    }

    @Override
    public void readStreamHeader() throws IOException {
      numKeysInThisBlock = -1;
      haveReadCurrentValues = false;
      nextKeyBlockOffset = 0;
      done = false;
    }

    @Override
    public IKeyBlock nextKeyBlock() {
      if(done) return null;

      try {
        // Skip values if possible.
        if (!haveReadCurrentValues) {
          stream.seek(nextKeyBlockOffset);
        }
        haveReadCurrentValues = false;
        // VByte: size of next block of keys+values
        int nextBlockSize = VByteCoders.ints.read(stream);
        nextKeyBlockOffset = stream.tell() + nextBlockSize;
        // Read in all the keys.
        List<Integer> keys = intsCoder.read(stream);
        this.numKeysInThisBlock = keys.size();
        return new KeyBlock(keys);

      } catch (IOException | CodecException e) {
        return null;
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
