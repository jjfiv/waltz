package jfoley.vocabpress.scoring.blockiter.movement;

import ciir.jfoley.chai.collections.util.ListFns;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley.
 */
public class AnyOfBlockMover implements BlockMover {

	private final List<BlockMover> children;
	protected int lastKey;
	protected List<Integer> currentBlock;
	protected int index;


	public AnyOfBlockMover(List<BlockMover> children) {
		this.children = ListFns.ensureRandomAccess(children);
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

		// find the first of the children's last keys ; the others need new blocks to be sure.
		// find the first of any child's keys.
		int lastKey = findLastKey();

		List<Integer> ids = new ArrayList<>();
		while(true) {
			int minimumChildKey = findMinimumKey();
			if(minimumChildKey == Mover.DONE_ID) {
				return;
			}

			// Add and move past the current key.
			ids.add(minimumChildKey);
			for (BlockMover child : children) {
				child.movePast(minimumChildKey);
			}

			if(minimumChildKey == lastKey) {
				for (BlockMover child : children) {
					assert(child.isDoneWithBlock() || child.currentKey() > minimumChildKey);
				}
				break;
			}
		}

		this.lastKey = lastKey;
		this.currentBlock = ids;
	}

	@Override
	public int maxKey() {
		return lastKey;
	}

	@Override
	public int currentKey() {
		if(isDone()) return Mover.DONE_ID;
		return currentBlock.get(index);
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
		for (BlockMover child : children) {
			assert(child.isDoneWithBlock() || child.currentKey() > lastKey);
		}

		for (int i = 0; i < children.size(); i++) {
			BlockMover child = children.get(i);
			//System.out.printf("nextBlock.%d.isDoneWithBlock=%s\n",i,child.isDoneWithBlock());


			child.movePast(lastKey);
			if (child.isDoneWithBlock()) {
				//System.out.println("isDoneWithBlock->nextBlock!");
				child.nextBlock();
			}
			assert (child.maxKey() > lastKey);
		}

		loadNewCurrentBlock();
	}

	@Override
	public void nextKey() {
		index++;
	}

	@Override
	public void moveTo(int key) {
		if(isDone()) return;
		if(key > lastKey) {
			index = currentBlock.size();
		}
		for(; index < currentBlock.size(); index++) {
			if(currentBlock.get(index) >= key) break;
		}
	}

	@Override
	public void movePast(int key) {
		moveTo(key+1);
	}
}
