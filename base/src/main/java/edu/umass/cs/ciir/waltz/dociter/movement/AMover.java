package edu.umass.cs.ciir.waltz.dociter.movement;

import ciir.jfoley.chai.fn.SinkFn;
import edu.umass.cs.ciir.waltz.dociter.IKeyBlock;
import edu.umass.cs.ciir.waltz.sys.KeyMetadata;

import javax.annotation.Nullable;

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
	public void execute(SinkFn<Integer> documentHandler) {
		for( ; !isDone(); nextBlock()) {
			int bs = currentBlock.size();
			for( ; index < bs; index++) {
				documentHandler.process(currentKey());
			}
		}
	}

	@Override
	public boolean isDone() {
		return currentBlock == null;
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
	public final void moveTo(int key) {
		if(isDoneWithBlock()) return;
		if(key > maxKey()) {
			index = currentBlock.size();
			return;
		}
		findInCurrentBlock(key);
	}

	public final void findInCurrentBlock(int key) {
		for(; index < currentBlock.size(); index++) {
			int current = currentBlock.getKey(index);
			if(current >= key) break;
		}
		assert(currentKey() >= key);
	}

	@Override
	public final void movePast(int key) {
		moveTo(key+1);
	}

  @Override
  public boolean matches(int key) {
    moveTo(key);
    return currentKey() == key;
  }

  /** Stay within block if possible, otherwise reset()! */
  @Override
  public void moveToAbsolute(int key) {
    int currentKey = currentKey();

		// it's here:
    if(key == currentKey) return;
		// it's forward:
    if(key > currentKey) {

			// skipBlocksTo:
			while(!isDone()) {
				moveTo(key);
				if(!isDoneWithBlock() && key <= currentKey()) { break; }
				nextBlock();
			};
    }

		// Otherwise, check if it's in this block:
    rewindBlock(key);
    if(key < currentKey()) {
      // TODO log a warning about slow resets needed.
      reset();
			// skipBlocksTo:
			while(!isDone()) {
				moveTo(key);
				if(!isDoneWithBlock() && key <= currentKey()) { break; }
				nextBlock();
			};
    } else {
			moveTo(key);
		}
  }

	@Override
	@Nullable
	public KeyMetadata<?> getMetadata() {
		return null;
	}
}
