package jfoley.vocabpress.scoring.blockiter.movement;

import jfoley.vocabpress.scoring.blockiter.IKeyBlock;
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

	protected IKeyBlock loadKeysFromChildren() {
		List<Integer> ids = new ArrayList<>();
		while(true) {
			int minimumChildKey = findMinimumKey();
			if(minimumChildKey == DONE_ID) {
				return null;
			}

			// Add and move past the current key.
			ids.add(minimumChildKey);
			for (BlockMover child : children) {
				child.movePast(minimumChildKey);
				assert(child.isDoneWithBlock() || child.currentKey() > minimumChildKey);
			}

			if(minimumChildKey == lastKey) {
				break;
			}
		}

		return new KeyBlock(ids);
	}
}
