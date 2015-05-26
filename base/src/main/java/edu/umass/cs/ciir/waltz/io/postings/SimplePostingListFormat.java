package edu.umass.cs.ciir.waltz.io.postings;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.data.MutableDataChunk;
import edu.umass.cs.ciir.waltz.coders.data.TmpFileDataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.DeltaIntListCoder;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;
import edu.umass.cs.ciir.waltz.dociter.IKeyBlock;
import edu.umass.cs.ciir.waltz.dociter.IValueBlock;
import edu.umass.cs.ciir.waltz.dociter.KeyBlock;
import edu.umass.cs.ciir.waltz.dociter.ValueBlock;
import edu.umass.cs.ciir.waltz.dociter.movement.BlockPostingsMover;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class SimplePostingListFormat {
  public static int DEFAULT_BLOCKSIZE = 128;
  public static Coder<List<Integer>> DEFAULT_INTSCODER = new DeltaIntListCoder();

  /**
   * This is a coder that reads and writes high level {:link PostingMover} objects.
   * @param <V> the type of value to put in the posting list.
   */
  public static class PostingCoder<V> extends PostingListCoder<V> {
    private final int blockSize;
    private final Coder<V> valCoder;
    private final Coder<List<Integer>> intsCoder;

    public PostingCoder(Coder<V> valCoder) throws IOException {
      this(DEFAULT_BLOCKSIZE, DEFAULT_INTSCODER, valCoder);
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
      ValueBuilder<V> writer = new PostingValueBuilder<>(blockSize, intsCoder, valCoder);
      writer.add(obj);
      return writer.getOutput();
    }

    @Override
    public PostingMover<V> read(StaticStream streamFn) throws IOException {
      return new BlockPostingsMover<>(new Reader<>(intsCoder, valCoder, streamFn));
    }
  }

  /**
   * This is a low-level builder that takes in sorted (key, value) pairs and writes them in chunks to a temporary file.
   *
   * @param <V>
   */
  public static class PostingValueBuilder<V> extends ValueBuilder<V> {
    /** The number of items to put in each block by default. */
    private final int blockSize;
    public PostingListChunk<V> currentChunk;
    MutableDataChunk output;
    public int totalKeys = 0;

    public PostingValueBuilder(Coder<V> valCoder) throws IOException {
      this(DEFAULT_BLOCKSIZE, DEFAULT_INTSCODER, valCoder);
    }
    public PostingValueBuilder(int blockSize, Coder<List<Integer>> intsCoder, Coder<V> valCoder) throws IOException {
      output = new TmpFileDataChunk();
      this.blockSize = blockSize;
      currentChunk = new PostingListChunk<>(intsCoder, valCoder);
    }

    @Override
    public void add(int key, V value) throws IOException {
      if(currentChunk.count() >= blockSize) { writeCurrentBlock(); }
      currentChunk.add(key, value);
      totalKeys++;
    }

    private void writeCurrentBlock() throws IOException {
      DataChunk data = currentChunk.encode();
      assert(data.byteCount() < Integer.MAX_VALUE);
      output.add(VarUInt.instance.write((int) data.byteCount()));
      output.add(data);
      currentChunk.clear();
    }

    /**
     * This is the output method of this builder, called when complete.
     * @return a DataChunk referencing the metadata, followed by the posting list itself, which is in a temporary file.
     */
    @Override
    public DataChunk getOutput() {
      if(currentChunk.count() > 0) {
        try {
          writeCurrentBlock();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      BufferList bl = new BufferList();
      bl.add(getMetadataChunk());
      bl.add(output);
      return bl;
    }

    public ByteBuffer getMetadataChunk() {
      // No matter what posting it is, it knows it's df.
      return VarUInt.instance.write(totalKeys);
    }
  }

  /**
   * A reader for postings with values of type V in this particular format.
   * @param <V>
   */
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
      totalKeys = VarUInt.instance.read(stream);
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
        int nextBlockSize = VarUInt.instance.read(stream);
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

  /**
   * A builder that collects a keyed set of values for writing to a file.
   * @param <V>
   */
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

    /**
     * Encode this data as a chunk for writing to a file.
     * @return the data in this chunk, keys first then values.
     */
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
