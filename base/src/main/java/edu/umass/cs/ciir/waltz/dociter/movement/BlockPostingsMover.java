package edu.umass.cs.ciir.waltz.dociter.movement;

import edu.umass.cs.ciir.waltz.dociter.BlockPostingsIterator;
import edu.umass.cs.ciir.waltz.dociter.IValueBlock;

/**
 * @author jfoley
 */
public class BlockPostingsMover<X> extends AMover implements PostingMover<X> {
  public final BlockPostingsIterator<X> iterator;
  private IValueBlock<X> valueBlock;

  public BlockPostingsMover(BlockPostingsIterator<X> iterator) {
    this.iterator = iterator;
    nextBlock();
  }

  @Override
  public X getCurrentPosting() {
    if(isDone()) return null;
    if(valueBlock == null) {
      valueBlock = iterator.nextValueBlock();
    }
    X val = valueBlock.getValue(index);
    return val;
  }

  @Override
  public void nextBlock() {
    this.currentBlock = iterator.nextKeyBlock();
    this.valueBlock = null;
    this.index = 0;
  }

  @Override
  public void reset() {
    iterator.reset();
    nextBlock();
  }

  @Override
  public int totalKeys() {
    return iterator.totalKeys();
  }
}
