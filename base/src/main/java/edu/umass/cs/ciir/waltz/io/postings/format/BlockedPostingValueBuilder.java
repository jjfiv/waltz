package edu.umass.cs.ciir.waltz.io.postings.format;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.data.MutableDataChunk;
import edu.umass.cs.ciir.waltz.coders.data.SmartDataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.io.postings.AbstractValueBuilder;

import java.io.IOException;
import java.util.List;

/**
 * This is a low-level builder that takes in sorted (key, value) pairs and writes them in chunks to a temporary file.
 *
 * @param <V>
 */
public class BlockedPostingValueBuilder<V> extends AbstractValueBuilder<V> {
  /** The number of items to put in each block by default. */
  private final int blockSize;
  public BlockedPostingListChunk<V> currentChunk;
  MutableDataChunk output;
  public int totalKeys = 0;
  public int maxKey = 0;

  public BlockedPostingValueBuilder(Coder<V> valCoder) throws IOException {
    this(BlockedPostingsFormat.DEFAULT_BLOCKSIZE, BlockedPostingsFormat.DEFAULT_INTSCODER, valCoder);
  }
  public BlockedPostingValueBuilder(int blockSize, Coder<List<Integer>> intsCoder, Coder<V> valCoder) throws IOException {
    output = new SmartDataChunk();
    this.blockSize = blockSize;
    currentChunk = new BlockedPostingListChunk<>(intsCoder, valCoder);
  }

  @Override
  public void add(int key, V value) throws IOException {
    if(currentChunk.count() >= blockSize) { writeCurrentBlock(); }
    currentChunk.add(key, value);
    totalKeys++;
    maxKey = Math.max(maxKey, key);
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

  public DataChunk getMetadataChunk() {
    // No matter what posting it is, it knows it's df.
    return VarUInt.instance.writeData(totalKeys);
  }

  @Override
  public void close() throws IOException {
    output.close();
  }
}
