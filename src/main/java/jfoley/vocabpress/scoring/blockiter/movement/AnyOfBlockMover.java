package jfoley.vocabpress.scoring.blockiter.movement;

import ciir.jfoley.chai.collections.util.ListFns;
import jfoley.vocabpress.scoring.blockiter.KeyBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author jfoley.
 */
public class AnyOfBlockMover extends ABlockMover {

	private final List<BlockMover> children;
	protected int lastKey;

	public AnyOfBlockMover(List<BlockMover> children) {
		this.children = ListFns.ensureRandomAccess(children);
		assert(children.size() == new HashSet<>(children).size());
		loadNewCurrentBlock();
	}

	private int findMinimumKey() {
		int minimumChildKey = children.get(0).currentKey();
		for (int i = 1; i < children.size(); i++) {
			BlockMover child = children.get(i);
			minimumChildKey = Math.min(minimumChildKey, child.currentKey());
		}
		//System.out.println("Found Minimum Key: "+minimumChildKey);
		return minimumChildKey;
	}

	private int findLastKey() {
		int lastKey = children.get(0).maxKey();
		for (int i = 1; i < children.size(); i++) {
			BlockMover child = children.get(i);
			lastKey = Math.min(lastKey, child.maxKey());
		}
		return lastKey;
	}

	private void loadNewCurrentBlock() {
		this.currentBlock = null;
		this.index = 0;
		this.lastKey = DONE_ID;

		// find the first of the children's last keys ; the others need new blocks to be sure.
		// find the first of any child's keys.
		int lastKey = findLastKey();
		if(lastKey == DONE_ID) {
			return;
		}

		int originalMinimum = findMinimumKey();

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
					child.rewind(originalMinimum); // reset this child so it can be used in another subtree!
				}
				break;
			}
		}

		this.lastKey = lastKey;
		this.currentBlock = new KeyBlock(ids);
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

	public static AnyOfBlockMover of(BlockMover... childs) {
		return new AnyOfBlockMover(Arrays.asList(childs));
	}
}
