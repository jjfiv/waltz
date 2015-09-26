package edu.umass.cs.ciir.waltz.dociter.movement;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.dociter.FastKeyBlock;
import edu.umass.cs.ciir.waltz.dociter.IKeyBlock;

import java.util.Arrays;
import java.util.List;

/**
 * @author jfoley.
 */
public class AnyOfMover<T extends Mover> extends AChildrenMover<T> {
	public AnyOfMover(List<T> children) {
		super(children);
	}

	public static <T extends Mover> AnyOfMover<T> of(T... childs) {
		return new AnyOfMover<>(Arrays.asList(childs));
	}

	protected IKeyBlock loadKeysFromChildren(int lastKey) {
		IntList ids = new IntList();
		int NC = children.size();

		int minimumChildKey = DONE_ID;
		for (int i = 0; i < NC; i++) {
			Mover child = children.get(i);
			minimumChildKey = Math.min(minimumChildKey, child.currentKey());
		}

		while(true) {
			//int minimumChildKey = findMinimumKey();
			if(minimumChildKey == DONE_ID) {
				// If the minimum key is the EOF marker, that means every child is done.
				if(ids.isEmpty()) return null;
				break;
			}

			int currentKey = minimumChildKey;
			// Add and move past the current key.
			ids.add(currentKey);
			minimumChildKey = DONE_ID;
			for (int i = 0; i < NC; i++) {
				Mover child = children.get(i);
				child.movePast(currentKey);
				minimumChildKey = Math.min(minimumChildKey, child.currentKey());
				assert (child.isDoneWithBlock() || child.currentKey() > currentKey);
			}

			if(currentKey >= lastKey) {
				break;
			}
		}

		return new FastKeyBlock(ids.unsafeArray(), ids.size());
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
