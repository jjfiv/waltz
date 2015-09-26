package edu.umass.cs.ciir.waltz.dociter.movement;

import ciir.jfoley.chai.collections.util.ListFns;
import edu.umass.cs.ciir.waltz.dociter.IKeyBlock;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * @author jfoley.
 */
public abstract class AChildrenMover<T extends Mover> extends AMover {
	protected final List<T> children;
	protected int lastKey;

	public AChildrenMover(List<T> children) {
		this.children = ListFns.ensureRandomAccess(children);
		assert(children.size() > 1);
		assert(children.size() == new HashSet<>(children).size());
		for (T child : children) {
			Objects.requireNonNull(child, "All children of movers must be non-null.");
		}
		loadNewCurrentBlock();
	}

  @Override
  public void reset() {
    for (Mover child : children) {
      child.reset();
    }
    loadNewCurrentBlock();
  }

	protected final int findLowestMax() {
		int lastKey = children.get(0).maxKey();
		for (int i = 1; i < children.size(); i++) {
			Mover child = children.get(i);
			lastKey = Math.min(lastKey, child.maxKey());
		}
		return lastKey;
	}

	protected final int findMinimumKey() {
		int minimumChildKey = children.get(0).currentKey();
		for (int i = 1; i < children.size(); i++) {
			Mover child = children.get(i);
			minimumChildKey = Math.min(minimumChildKey, child.currentKey());
		}
		return minimumChildKey;
	}

	protected final int findHighestMinimum() {
		int highMin = children.get(0).currentKey();
		for (int i = 1; i < children.size(); i++) {
			Mover child = children.get(i);
			highMin = Math.max(highMin, child.currentKey());
		}
		return highMin;
	}

	@Override
	public void nextBlock() {
		if(lastKey == DONE_ID) return;
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

		while(true) {
			// find the first of any child's maximum keys.
			int lastKey = findLowestMax();
			if (lastKey == DONE_ID) {
				return;
			}

			this.lastKey = lastKey;
			this.currentBlock = loadKeysFromChildren(lastKey);
			if(this.currentBlock == null) {
				return; // End of List
			}
			if(!this.currentBlock.isEmpty()) {
				break;
			}

			// No matches
			for (T child : this.children) {
				if(child.isDoneWithBlock()) {
					child.nextBlock();
				}
			}
		}

		// rewind so values can be stepped through.
		for (T child : children) {
			int here = this.currentKey();
			child.rewindBlock(here);
		}
	}

	/**
	 * Select matching from child blocks
	 * @param lastKey is the min(x.maxKey for x in children); the upper bound of current block.
	 * @return a block filled with matches between here and lastKey inclusive.
	 */
	protected abstract IKeyBlock loadKeysFromChildren(int lastKey);

}
