package edu.umass.cs.ciir.waltz.io.postings.format;

import ciir.jfoley.chai.IntMath;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.files.DataSink;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;

import java.io.IOException;
import java.util.List;

/**
 * @author jfoley
 */
public class BlockedPostingValueWriter<V> {
  /** The number of items to put in each block by default. */
  private final int blockSize;
  public BlockedPostingListChunk<V> currentChunk;
  public final DataSink output;

  public BlockedPostingValueWriter(DataSink output, Coder<V> valCoder) throws IOException {
    this(output, BlockedPostingsFormat.DEFAULT_BLOCKSIZE, BlockedPostingsFormat.DEFAULT_INTSCODER, valCoder);
  }
  public BlockedPostingValueWriter(DataSink output, int blockSize, Coder<List<Integer>> intsCoder, Coder<V> valCoder) throws IOException {
    this.output = output;
    this.blockSize = blockSize;
    currentChunk = new BlockedPostingListChunk<>(intsCoder, valCoder);
  }

  public void add(int key, V value) throws IOException {
    if(currentChunk.count() >= blockSize) { writeCurrentBlock(); }
    currentChunk.add(key, value);
  }

  private void writeCurrentBlock() throws IOException {
    DataChunk data = currentChunk.encode();
    output.write(VarUInt.instance.write(IntMath.fromLong(data.byteCount())));
    output.write(data);
    currentChunk.clear();
  }

  public void finish() throws IOException {
    if(currentChunk.count() > 0) {
      writeCurrentBlock();
    }
  }
}
