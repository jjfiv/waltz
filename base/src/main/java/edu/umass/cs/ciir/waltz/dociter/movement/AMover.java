package edu.umass.cs.ciir.waltz.dociter.movement;

import edu.umass.cs.ciir.waltz.dociter.IKeyBlock;

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
		while(isDoneWithBlock()) {
			if (isDone()) break;
			if (!isDone()) {
				nextBlock();
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
	public void rewindBlock(int value) {
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

  @Override
  public boolean matches(int key) {
    return currentKey() == key;
  }

  /** Stay within block if possible, otherwise reset()! */
  @Override
  public void moveToAbsolute(int key) {
    int currentKey = currentKey();
    if(key == currentKey) return;
    if(key >= currentKey) {
      moveTo(key);
      return;
    }
    rewindBlock(key);
    if(key < currentKey()) {
      // TODO log a warning about slow resets needed.
      reset();
      moveTo(key);
    }
  }
}
