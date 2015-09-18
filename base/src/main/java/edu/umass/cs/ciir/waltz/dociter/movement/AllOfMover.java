package edu.umass.cs.ciir.waltz.dociter.movement;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.dociter.FastKeyBlock;
import edu.umass.cs.ciir.waltz.dociter.IKeyBlock;

import java.util.Arrays;
import java.util.List;

/**
 * @author jfoley.
 */
public class AllOfMover<T extends Mover> extends AChildrenMover<T> {

	public static <T extends Mover> AllOfMover of(T... childs) {
		return new AllOfMover<>(Arrays.asList(childs));
	}
	public AllOfMover(List<T> children) {
		super(children);
	}

	private int findMaxCurrentKey() {
		int maxCurrent = children.get(0).currentKey();
		for (int i = 1; i < children.size(); i++) {
			Mover child = children.get(i);
			maxCurrent = Math.max(child.currentKey(), maxCurrent);
		}

		return maxCurrent;
	}

	private boolean isMatch(int targetKey) {
		for (Mover child : children) {
			child.moveTo(targetKey);
			if(child.currentKey() != targetKey) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected IKeyBlock loadKeysFromChildren(int lastKey) {
		// remember, lastKey is the farthest we know we've loaded for all of the children:
		IntList ids = new IntList();
		while(true) {
			int firstPossibleMatch = findMaxCurrentKey();
			//System.out.println(Arrays.asList("firstPossibleMatch", firstPossibleMatch, "lastPossibleMatch", lastKey));
			if(firstPossibleMatch == DONE_ID) {
				// if we have some matches, return them for now; otherwise, done for good.
				if(ids.isEmpty()) return null; else break;
			} else if (firstPossibleMatch > lastKey) {
				for (Mover child : children) {
					child.moveTo(firstPossibleMatch);
					assert(child.isDoneWithBlock() || child.currentKey() >= firstPossibleMatch);
				}
				break;
			}

			// Add and move past the current key.
			if(isMatch(firstPossibleMatch)) {
				ids.add(firstPossibleMatch);
			}

			for (Mover child : children) {
				child.movePast(firstPossibleMatch);
				assert(child.isDoneWithBlock() || child.currentKey() > firstPossibleMatch);
			}

			if(firstPossibleMatch >= lastKey) {
				break;
			}
		}

		return new FastKeyBlock(ids.unsafeArray(), ids.size());
	}

  @Override
  public int totalKeys() {
    int estimate = children.get(0).totalKeys();
    for (int i = 1; i < children.size(); i++) {
      estimate = Math.min(estimate, children.get(i).totalKeys());
    }
    return estimate;
  }
}
