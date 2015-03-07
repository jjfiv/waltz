package jfoley.vocabpress.scoring.blockiter.movement;

import jfoley.vocabpress.scoring.Posting;
import jfoley.vocabpress.scoring.blockiter.BlockPostingsIterator;
import jfoley.vocabpress.scoring.blockiter.IKeyBlock;
import jfoley.vocabpress.scoring.blockiter.IValueBlock;

/**
 * @author jfoley.
 */
public class FeatureBlockMover<X extends Posting> implements BlockMover, Feature<X> {
	private final BlockPostingsIterator<X> iter;
	private IKeyBlock currentBlock;
	private IValueBlock<X> valueBlock;
	private int index;

	public FeatureBlockMover(BlockPostingsIterator<X> iter) {
		this.iter = iter;
		nextBlock();
	}

	@Override
	public int maxKey() {
		if(isDone()) return Mover.DONE_ID;
		return currentBlock.maxKey();
	}

	@Override
	public int currentKey() {
		if(isDone()) return Mover.DONE_ID;
		if(isDoneWithBlock()) return maxKey()+1;
		return currentBlock.getKey(index);
	}

	@Override
	public boolean isDone() {
		return currentBlock == null;
	}

	@Override
	public boolean isDoneWithBlock() {
		return isDone() || index >= currentBlock.size();
	}

	@Override
	public void nextBlock() {
		this.currentBlock = iter.nextKeyBlock();
		this.valueBlock = null;
		this.index = 0;
	}

	@Override
	public void nextKey() {
		this.index++;
	}

	@Override
	public void moveTo(int key) {
		if(isDoneWithBlock()) return;
		for(; index < currentBlock.size(); index++) {
			if(currentBlock.getKey(index) >= key) break;
		}
		if(currentKey() < key) {
			System.out.println("currentKey:"+currentKey());
			System.out.println("key:"+key);
			System.out.println("index:"+index);
			System.out.println("currentBlock:"+currentBlock);
			System.out.println("currentBlock.size:"+currentBlock.size());
		}
		assert(currentKey() >= key);
	}

	@Override
	public void movePast(int key) {
		if(isDoneWithBlock()) return;
		moveTo(key+1);
		assert(currentKey() > key);
	}

	@Override
	public void rewind() {
		if(isDone()) return;
		index = 0;
		assert(currentKey() == currentBlock.getKey(0));
	}

	@Override
	public boolean hasFeature(int key) {
		return !isDone() && !isDoneWithBlock() && currentKey() == key;
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
}
