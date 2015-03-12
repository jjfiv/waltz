package jfoley.vocabpress.dociter.movement;

import ciir.jfoley.chai.collections.util.ListFns;
import jfoley.vocabpress.dociter.IKeyBlock;

import java.util.HashSet;
import java.util.List;

/**
 * @author jfoley.
 */
public abstract class AChildrenMover extends AMover {
	protected final List<Mover> children;
	protected int lastKey;

	public AChildrenMover(List<? extends Mover> children) {
		this.children = ListFns.ensureRandomAccess(children);
		assert(children.size() == new HashSet<>(children).size());
		loadNewCurrentBlock();
	}

  @Override
  public void reset() {
    for (Mover child : children) {
      child.reset();
    }
    loadNewCurrentBlock();
  }

	protected int findLastKey() {
		int lastKey = children.get(0).maxKey();
		for (int i = 1; i < children.size(); i++) {
			Mover child = children.get(i);
			lastKey = Math.min(lastKey, child.maxKey());
		}
		return lastKey;
	}

	protected int findMinimumKey() {
		int minimumChildKey = children.get(0).currentKey();
		for (int i = 1; i < children.size(); i++) {
			Mover child = children.get(i);
			minimumChildKey = Math.min(minimumChildKey, child.currentKey());
		}
		return minimumChildKey;
	}

	@Override
	public void nextBlock() {
		for (Mover child : children) {
			child.movePast(lastKey);
			assert (child.isDoneWithBlock() || child.currentKey() > lastKey);

			if (child.isDoneWithBlock()) {
				child.nextBlock();
			}
		}
		loadNewCurrentBlock();
	}

	protected void loadNewCurrentBlock() {
		this.currentBlock = null;
		this.index = 0;
		this.lastKey = DONE_ID;

		// find the first of any child's keys.
		int lastKey = findLastKey();
		if(lastKey == DONE_ID) {
			return;
		}

		int originalMinimum = findMinimumKey();
		this.lastKey = lastKey;
		this.currentBlock = loadKeysFromChildren();

		for (Mover child : children) {
			//assert(child.isDoneWithBlock() || child.currentKey() > lastKey);
			child.rewindBlock(originalMinimum); // reset this child so it can be used in another subtree!
		}
	}

	protected abstract IKeyBlock loadKeysFromChildren();

}
