package edu.umass.cs.ciir.waltz.sys;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.coders.CoderException;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;
import edu.umass.cs.ciir.waltz.dociter.FastKeyBlock;
import edu.umass.cs.ciir.waltz.dociter.IKeyBlock;
import edu.umass.cs.ciir.waltz.dociter.IValueBlock;
import edu.umass.cs.ciir.waltz.dociter.ValueBlock;
import edu.umass.cs.ciir.waltz.io.postings.StaticStreamPostingsIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jfoley
 */
public class MetadataBlockPostingsIterator<M extends KeyMetadata<V, M>, V> extends StaticStreamPostingsIterator<V> {
  private final PostingsConfig<?, M, V> cfg;
  private boolean haveReadCurrentValues;
  private long nextKeyBlockOffset;
  private int numKeysInThisBlock;
  private int usedKeys = 0;
  private boolean done;
  IntList keys;
  private M metadata;

  public MetadataBlockPostingsIterator(PostingsConfig<?, M, V> cfg, StaticStream streamSource) {
    super(streamSource);
    this.cfg = Objects.requireNonNull(cfg);
    keys = new IntList();
    metadata = null;
    try {
      this.readStreamHeader();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void readStreamHeader() throws IOException {
    numKeysInThisBlock = -1;
    haveReadCurrentValues = true;
    nextKeyBlockOffset = 0;
    done = false;

    this.metadata = cfg.metadataCoder.read(stream);

    long remaining = streamSource.length() - stream.tell();
    // should be at least 1 byte left for each key:
    assert (remaining >= metadata.totalDocuments());
    usedKeys = 0;
  }

  @Override
  public IKeyBlock nextKeyBlock() {
    if (done) return null;
    if (usedKeys == totalKeys()) return null;

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
      cfg.docsCoder.readInto(keys, stream);
      this.numKeysInThisBlock = keys.size();
      usedKeys += numKeysInThisBlock;
      return new FastKeyBlock(keys.unsafeArray(), keys.size());
    } catch (IOException | CoderException e) {
      try {
        System.err.println("tell: " + stream.tell());
        System.err.println(usedKeys);
        System.err.println(totalKeys());
        System.err.println(metadata);
        System.err.println("tell: " + stream.tell());
        System.err.println("length: " + streamSource.length());
      } catch (IOException e1) {
        throw new RuntimeException(e1);
      }
      throw new RuntimeException(e);
    }
  }

  @Override
  public IValueBlock<V> nextValueBlock() {
    if (done) return null;
    haveReadCurrentValues = true;

    List<V> values = new ArrayList<>(numKeysInThisBlock);
    for (int i = 0; i < numKeysInThisBlock; i++) {
      values.add(cfg.valCoder.read(stream));
    }

    return new ValueBlock<>(values);
  }

  @Override
  public int totalKeys() {
    return metadata.totalDocuments();
  }
}
