package jfoley.vocabpress.movement;

import jfoley.vocabpress.scoring.blockiter.KeyBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author jfoley
 */
public class IdSetMover extends AMover {
  private final List<Integer> keys;
  private int readPtr;
  private int blockSize;

  public IdSetMover(Set<Integer> keys, int blockSize) {
    List<Integer> skeys = new ArrayList<>(keys);
    Collections.sort(skeys);
    this.keys = skeys;
    this.blockSize = blockSize;
    this.readPtr = 0;

    nextBlock();
  }

  public IdSetMover(Set<Integer> keys) {
    this(keys, 16);
  }

  @Override
  public void nextBlock() {
    this.currentBlock = null;
    this.index = 0;

    if(readPtr >= keys.size()) {
      return;
    }

    List<Integer> relevantKeys = keys.subList(readPtr, Math.min(readPtr + blockSize, keys.size()));
    this.currentBlock = new KeyBlock(relevantKeys);
    readPtr += blockSize;
  }
}

