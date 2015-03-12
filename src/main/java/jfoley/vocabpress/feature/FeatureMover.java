package jfoley.vocabpress.feature;

import jfoley.vocabpress.dociter.movement.AMover;
import jfoley.vocabpress.dociter.movement.Mover;
import jfoley.vocabpress.postings.Posting;
import jfoley.vocabpress.dociter.BlockPostingsIterator;
import jfoley.vocabpress.dociter.IValueBlock;

/**
 * @author jfoley.
 */
public class FeatureMover<X extends Posting> extends AMover implements Feature<X> {
	private final BlockPostingsIterator<X> iter;
	private IValueBlock<X> valueBlock;

	public FeatureMover(BlockPostingsIterator<X> iter) {
		this.iter = iter;
		nextBlock();
	}

	@Override
	public void nextBlock() {
		this.currentBlock = iter.nextKeyBlock();
		this.valueBlock = null;
		this.index = 0;
	}

	@Override
	public boolean hasFeature(int key) {
		return !isDoneWithBlock() && currentKey() == key;
	}

	@Override
	public X getFeature(int key) {
		if(key == currentKey()) {
			if(valueBlock == null) {
				valueBlock = iter.nextValueBlock();
			}
			return valueBlock.getValue(index);
		}
		return null;
	}

  @Override
  public Mover getMover() {
    return this;
  }
}
