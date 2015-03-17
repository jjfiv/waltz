package edu.umass.cs.ciir.waltz.dociter.movement;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.dociter.IKeyBlock;
import edu.umass.cs.ciir.waltz.dociter.KeyBlock;

import java.util.Arrays;
import java.util.List;

/**
 * @author jfoley.
 */
public class AllOfMover extends AChildrenMover {

	public static AllOfMover of(Mover... childs) {
		return new AllOfMover(Arrays.asList(childs));
	}
	public AllOfMover(List<Mover> children) {
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

	@Override
	protected IKeyBlock loadKeysFromChildren() {
		List<Integer> ids = new IntList();
		while(true) {
			int targetKey = findMaxCurrentKey();
			if(targetKey == DONE_ID) {
				return null;
			} else if(targetKey > lastKey) {
				break;
			}

			// See if everyone has that key:
			boolean isHit = true;
			for (Mover child : children) {
				child.moveTo(targetKey);
				if(child.currentKey() != targetKey) {
					isHit = false;
					break;
				}
			}

			if(isHit) {
				ids.add(targetKey);
			}

			// Add and move past the current key.
			for (Mover child : children) {
				child.movePast(targetKey);
				assert(child.isDoneWithBlock() || child.currentKey() > targetKey);
			}

			if(targetKey >= lastKey) {
				break;
			}
		}

		//System.out.println(ids+"\t"+lastKey);
		return new KeyBlock(ids);
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
