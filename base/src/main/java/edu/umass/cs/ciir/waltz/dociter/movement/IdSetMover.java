package edu.umass.cs.ciir.waltz.dociter.movement;

import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.collections.util.ArrayFns;
import edu.umass.cs.ciir.waltz.dociter.KeyBlock;

import java.util.*;

/**
 * @author jfoley
 */
public class IdSetMover extends AMover {
  private final List<Integer> keys;
  private int readPtr;
  private int blockSize;

  public IdSetMover(Collection<Integer> keys, int blockSize) {
    List<Integer> skeys = new ArrayList<>(keys);
    Collections.sort(skeys);
    this.keys = new IntList(skeys);
    this.blockSize = blockSize;
    this.readPtr = 0;

    nextBlock();
  }

  public IdSetMover(Collection<Integer> keys) {
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

  @Override
  public void reset() {
    readPtr = 0;
    nextBlock();
  }

  public static IdSetMover of(int... i) {
		return new IdSetMover(new HashSet<>(ArrayFns.toList(i)));
	}
}

