package jfoley.vocabpress.scoring.blockiter.movement;

import java.util.Arrays;
import java.util.List;

/**
 * @author jfoley.
 */
public class AllOfBlockMover extends AChildrenBlockMover {

	public static AllOfBlockMover of(BlockMover... childs) {
		return new AllOfBlockMover(Arrays.asList(childs));
	}
	public AllOfBlockMover(List<BlockMover> children) {
		super(children);
	}

	protected void loadNewCurrentBlock() {
		this.currentBlock = null;
		this.index = 0;
		this.lastKey = DONE_ID;

		// find the first of the children's last keys ; the others need new blocks to be sure.
		// find the first of any child's keys.
		int lastKey = findLastKey();
		if(lastKey == DONE_ID) {
			return;
		}
	}

}
