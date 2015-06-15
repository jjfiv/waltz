package edu.umass.cs.ciir.waltz.io.postings.format;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;
import edu.umass.cs.ciir.waltz.dociter.IKeyBlock;
import edu.umass.cs.ciir.waltz.dociter.IValueBlock;
import edu.umass.cs.ciir.waltz.dociter.KeyBlock;
import edu.umass.cs.ciir.waltz.dociter.ValueBlock;
import edu.umass.cs.ciir.waltz.io.postings.StaticStreamPostingsIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A reader for postings with values of type V in this particular format.
 * @param <V>
 */
public class Reader<V> extends StaticStreamPostingsIterator<V> {
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
