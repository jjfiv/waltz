package edu.umass.cs.ciir.waltz.postings.docset;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;
import edu.umass.cs.ciir.waltz.dociter.KeyBlock;
import edu.umass.cs.ciir.waltz.dociter.movement.AMover;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class DeltaIntListStreamMover extends AMover {
  final StaticStream streamFn;
  InputStream stream;
  int total = 0;
  int currentIndex = 0;
  int previousValue = 0;
  final Coder<Integer> itemCoder;
  final Coder<Integer> countCoder;

  public DeltaIntListStreamMover(StaticStream streamFn) {
    this(streamFn, VarUInt.instance, VarUInt.instance);
  }

  public DeltaIntListStreamMover(StaticStream streamFn, Coder<Integer> countCoder, Coder<Integer> itemCoder) {
    this.streamFn = streamFn;
    this.itemCoder = itemCoder;
    this.countCoder = countCoder;
    reset();
  }

  @Override
  public void nextBlock() {
    this.currentBlock = null;
    this.index = 0;

    IntList block = new IntList();
    int end = Math.min(total, currentIndex + 128);
    try {
      for (; currentIndex < end; currentIndex++) {
        previousValue += itemCoder.read(stream);
        block.add(previousValue);
      }
      if (block.isEmpty()) {
        return;
      }
      currentBlock = new KeyBlock(block);
    } catch (Exception e) {
      System.out.printf("total: %d, end: %d\n", total, end);
      throw e;
    }
  }

  @Override
  public void reset() {
    try {
      stream = streamFn.getNewStream();
      total = countCoder.read(stream);
      currentIndex = 0;
      previousValue = 0;
      nextBlock();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int totalKeys() {
    return total;
  }
}
