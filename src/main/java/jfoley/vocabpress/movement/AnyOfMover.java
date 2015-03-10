package jfoley.vocabpress.movement;

import ciir.jfoley.chai.collections.list.IntList;
import jfoley.vocabpress.scoring.blockiter.IKeyBlock;
import jfoley.vocabpress.scoring.blockiter.KeyBlock;

import java.util.Arrays;
import java.util.List;

/**
 * @author jfoley.
 */
public class AnyOfMover extends AChildrenMover {
	public AnyOfMover(List<Mover> children) {
		super(children);
	}

	public static AnyOfMover of(Mover... childs) {
		return new AnyOfMover(Arrays.asList(childs));
	}

	protected IKeyBlock loadKeysFromChildren() {
		List<Integer> ids = new IntList();
		while(true) {
			int minimumChildKey = findMinimumKey();
			if(minimumChildKey == DONE_ID) {
				return null;
			}

			// Add and move past the current key.
			ids.add(minimumChildKey);
			for (Mover child : children) {
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
