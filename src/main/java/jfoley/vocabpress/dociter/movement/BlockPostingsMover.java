package jfoley.vocabpress.dociter.movement;

import jfoley.vocabpress.dociter.BlockPostingsIterator;
import jfoley.vocabpress.dociter.IValueBlock;
import jfoley.vocabpress.postings.Posting;

/**
 * @author jfoley
 */
public class BlockPostingsMover<X extends Posting> extends AMover implements PostingMover<X> {
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
    assert(val.getKey() == currentKey());
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
}
