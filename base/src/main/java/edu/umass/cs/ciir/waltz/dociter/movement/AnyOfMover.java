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

	protected IKeyBlock loadKeysFromChildren() {
		IntList ids = new IntList();
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

		return new FastKeyBlock(ids.asArray(), ids.size());
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
