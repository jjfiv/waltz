package jfoley.vocabpress.movement;

import jfoley.vocabpress.scoring.blockiter.IKeyBlock;

/**
 * @author jfoley.
 */
public abstract class AMover implements Mover {
	protected IKeyBlock currentBlock;
	protected int index;

	@Override
	public int maxKey() {
		if(isDone()) return DONE_ID;
		return currentBlock.maxKey();
	}

	@Override
	public boolean isDoneWithBlock() {
		return isDone() || index >= currentBlock.size();
	}

	@Override
	public boolean isDone() {
		return currentBlock == null;
	}

	@Override
	public void next() {
		if(!isDoneWithBlock()) {
			nextKey();
		}
		if(isDoneWithBlock()) {
			if (!isDone()) {
				nextBlock();
				return;
			}
		}
	}

	@Override
	public int currentKey() {
		if(isDone()) return DONE_ID;
		if(isDoneWithBlock()) return maxKey()+1;
		return currentBlock.getKey(index);
	}

	@Override
	public void nextKey() {
		this.index++;
	}

	@Override
	public void rewind(int value) {
		if(isDone()) return;
		index = 0;
		moveTo(value);
	}

	@Override
	public void moveTo(int key) {
		if(isDoneWithBlock()) return;
		if(key > maxKey()) {
			index = currentBlock.size();
			return;
		}
		for(; index < currentBlock.size(); index++) {
			if(currentKey() >= key) break;
		}
		assert(currentKey() >= key);
	}

	@Override
	public void movePast(int key) {
		if(isDoneWithBlock()) return;
		moveTo(key+1);
		assert(currentKey() > key);
	}
}
