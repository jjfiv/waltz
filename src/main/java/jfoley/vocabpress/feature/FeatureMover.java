package jfoley.vocabpress.feature;

import jfoley.vocabpress.movement.AMover;
import jfoley.vocabpress.movement.Mover;
import jfoley.vocabpress.scoring.Posting;
import jfoley.vocabpress.scoring.blockiter.BlockPostingsIterator;
import jfoley.vocabpress.scoring.blockiter.IValueBlock;

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
