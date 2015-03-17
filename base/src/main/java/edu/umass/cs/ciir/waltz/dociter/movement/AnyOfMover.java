package edu.umass.cs.ciir.waltz.dociter.movement;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.dociter.IKeyBlock;
import edu.umass.cs.ciir.waltz.dociter.KeyBlock;

import java.util.Arrays;
import java.util.List;

/**
 * @author jfoley.
 */
public class AnyOfMover extends AChildrenMover {
	public AnyOfMover(List<? extends Mover> children) {
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

  @Override
  public int totalKeys() {
    int estimate = 0;
    for (Mover aChild : children) {
      estimate += aChild.totalKeys();
    }
    return estimate;
  }
}
