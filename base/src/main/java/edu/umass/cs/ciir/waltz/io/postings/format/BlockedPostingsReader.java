package edu.umass.cs.ciir.waltz.io.postings.format;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.CoderException;
import edu.umass.cs.ciir.waltz.coders.ints.IntsCoder;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;
import edu.umass.cs.ciir.waltz.dociter.*;
import edu.umass.cs.ciir.waltz.io.postings.StaticStreamPostingsIterator;
import edu.umass.cs.ciir.waltz.sys.KeyMetadata;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A reader for postings with values of type V in this particular format.
 * @param <V>
 */
public class BlockedPostingsReader<V> extends StaticStreamPostingsIterator<V> {
  private final IntsCoder intsCoder;
  private final Coder<V> valCoder;
  private boolean haveReadCurrentValues;
  private long nextKeyBlockOffset;
  private int numKeysInThisBlock;
  private int totalKeys;
  private int usedKeys = 0;
  private boolean done;
  IntList keys;

  public BlockedPostingsReader(IntsCoder intsCoder, Coder<V> valCoder, StaticStream streamSource) {
    super(streamSource);
    this.intsCoder = intsCoder;
    this.valCoder = valCoder;
    keys = new IntList();
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
    totalKeys = VarUInt.instance.read(stream);

    long remaining = streamSource.length() - stream.tell();
    // should be at least 1 byte left for each key:
    assert(remaining >= totalKeys) : "totalKeys: "+totalKeys+" bytes remaining: "+remaining;
    usedKeys = 0;
  }

  @Nullable
  @Override
  public KeyMetadata<V> getMetadata() {
    // TODO: return a fake metadata object that knows totalKeys
    return null;
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
      intsCoder.readInto(keys, stream);
      this.numKeysInThisBlock = keys.size();
      usedKeys += numKeysInThisBlock;
      return new FastKeyBlock(keys.unsafeArray(), keys.size());
    } catch (IOException | CoderException e) {
      try {
        System.err.println("tell: "+stream.tell());
        System.err.println(usedKeys);
        System.err.println(totalKeys);
        System.err.println("tell: "+stream.tell());
        System.err.println("length: "+streamSource.length());
      } catch (IOException e1) {
        throw new RuntimeException(e1);
      }
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
