package jfoley.vocabpress.scoring.blockiter.movement;

import ciir.jfoley.chai.collections.util.ListFns;

import java.util.HashSet;
import java.util.List;

/**
 * @author jfoley.
 */
public abstract class AChildrenBlockMover extends ABlockMover {
	protected final List<BlockMover> children;
	protected int lastKey;

	public AChildrenBlockMover(List<BlockMover> children) {
		this.children = ListFns.ensureRandomAccess(children);
		assert(children.size() == new HashSet<>(children).size());
		loadNewCurrentBlock();
	}

	protected int findLastKey() {
		int lastKey = children.get(0).maxKey();
		for (int i = 1; i < children.size(); i++) {
			BlockMover child = children.get(i);
			lastKey = Math.min(lastKey, child.maxKey());
		}
		return lastKey;
	}

	protected int findMinimumKey() {
		int minimumChildKey = children.get(0).currentKey();
		for (int i = 1; i < children.size(); i++) {
			BlockMover child = children.get(i);
			minimumChildKey = Math.min(minimumChildKey, child.currentKey());
		}
		return minimumChildKey;
	}

	@Override
	public void nextBlock() {
		for (BlockMover child : children) {
			child.movePast(lastKey);
			assert (child.isDoneWithBlock() || child.currentKey() > lastKey);

			if (child.isDoneWithBlock()) {
				child.nextBlock();
			}
		}
		loadNewCurrentBlock();
	}

	protected abstract void loadNewCurrentBlock();

}
