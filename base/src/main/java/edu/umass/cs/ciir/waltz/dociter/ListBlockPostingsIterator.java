package edu.umass.cs.ciir.waltz.dociter;

import edu.umass.cs.ciir.waltz.postings.Posting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class ListBlockPostingsIterator<X> implements BlockPostingsIterator<X> {
  private final List<Posting<X>> postings;
  private final int blockSize;
  private int keyReadPtr;
  private int valueReadPtr;

  public ListBlockPostingsIterator(List<Posting<X>> postings) {
    this(postings, 16);
  }
  public ListBlockPostingsIterator(List<Posting<X>> postings, int blockSize) {
    this.postings = postings;
    this.blockSize = blockSize;
    this.keyReadPtr = 0;
    this.valueReadPtr = 0;
  }

  @Override
  public FastKeyBlock nextKeyBlock() {
    int[] bufferedKeys = new int[blockSize];
    int writeIndex;
    for (writeIndex = 0; writeIndex < blockSize && (keyReadPtr+writeIndex) < postings.size(); writeIndex++) {
      bufferedKeys[writeIndex] = postings.get(keyReadPtr+writeIndex).getKey();
    }
    // skip any non-decoded values.
    valueReadPtr = keyReadPtr;
    keyReadPtr += bufferedKeys.length;

    if(writeIndex == 0) return null;
    return new FastKeyBlock(bufferedKeys, writeIndex);
  }

  @Override
  public ValueBlock<X> nextValueBlock() {
    List<X> bufferedVals = new ArrayList<>();
    for (int i = 0; i < blockSize && (valueReadPtr+i) < postings.size(); i++) {
      bufferedVals.add(postings.get(valueReadPtr + i).getValue());
    }
    valueReadPtr += bufferedVals.size();
    return new ValueBlock<>(bufferedVals);
  }

  @Override
  public void reset() {
    keyReadPtr = valueReadPtr = 0;
  }

  @Override
  public int totalKeys() {
    return postings.size();
  }
}
