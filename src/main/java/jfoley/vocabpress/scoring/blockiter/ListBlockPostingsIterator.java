package jfoley.vocabpress.scoring.blockiter;

import jfoley.vocabpress.postings.Posting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class ListBlockPostingsIterator<X extends Posting> implements BlockPostingsIterator<X> {
  private final List<X> postings;
  private final int blockSize;
  private int keyReadPtr;
  private int valueReadPtr;

  public ListBlockPostingsIterator(List<X> postings) {
    this(postings, 16);
  }
  public ListBlockPostingsIterator(List<X> postings, int blockSize) {
    this.postings = postings;
    this.blockSize = blockSize;
    this.keyReadPtr = 0;
    this.valueReadPtr = 0;
  }

  @Override
  public KeyBlock nextKeyBlock() {
    List<Integer> bufferedKeys = new ArrayList<>();
    for (int i = 0; i < blockSize && (keyReadPtr+i) < postings.size(); i++) {
      bufferedKeys.add(postings.get(keyReadPtr+i).getKey());
    }
    // skip any non-decoded values.
    valueReadPtr = keyReadPtr;
    keyReadPtr += bufferedKeys.size();

    if(bufferedKeys.isEmpty()) return null;
    return new KeyBlock(bufferedKeys);
  }

  @Override
  public ValueBlock<X> nextValueBlock() {
    List<X> bufferedVals = new ArrayList<>();
    for (int i = 0; i < blockSize && (valueReadPtr+i) < postings.size(); i++) {
      bufferedVals.add(postings.get(valueReadPtr + i));
    }
    valueReadPtr += bufferedVals.size();
    return new ValueBlock<>(bufferedVals);
  }
}
