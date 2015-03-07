package jfoley.vocabpress.scoring.blockiter.movement;

import jfoley.vocabpress.scoring.blockiter.KeyBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jfoley.
 */
public class AnyOfBlockMover extends AChildrenBlockMover {
	public AnyOfBlockMover(List<BlockMover> children) {
		super(children);
	}

	public static AnyOfBlockMover of(BlockMover... childs) {
		return new AnyOfBlockMover(Arrays.asList(childs));
	}

	protected void loadKeysFromChildren(int minKey, int lastKey) {
		List<Integer> ids = new ArrayList<>();
		while(true) {
			int minimumChildKey = findMinimumKey();
			if(minimumChildKey == DONE_ID) {
				return;
			}

			// Add and move past the current key.
			ids.add(minimumChildKey);
			for (BlockMover child : children) {
				child.movePast(minimumChildKey);
				assert(child.isDoneWithBlock() || child.currentKey() > minimumChildKey);
			}

			if(minimumChildKey == lastKey) {
				for (BlockMover child : children) {
					assert(child.isDoneWithBlock() || child.currentKey() > minimumChildKey);
					child.rewind(minKey); // reset this child so it can be used in another subtree!
				}
				break;
			}
		}

		this.lastKey = lastKey;
		this.currentBlock = new KeyBlock(ids);
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
		loadKeysFromChildren(originalMinimum, lastKey);
		assert(this.lastKey == lastKey);
		assert(this.currentBlock != null);
	}

}
