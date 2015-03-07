package jfoley.vocabpress.scoring.blockiter.movement;

/**
 * @author jfoley.
 */
public abstract class ABlockMover implements BlockMover {
	protected int index;

	public abstract int blockSize();

	@Override
	public boolean isDoneWithBlock() {
		return isDone() || index >= blockSize();
	}

	@Override
	public void nextKey() {
		this.index++;
	}

	@Override
	public void rewind() {
		if(isDone()) return;
		index = 0;
	}

	@Override
	public void moveTo(int key) {
		if(isDoneWithBlock()) return;
		if(key > maxKey()) {
			index = blockSize();
			return;
		}
		for(; index < blockSize(); index++) {
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
