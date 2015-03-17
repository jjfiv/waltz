package edu.umass.cs.ciir.waltz.dociter;

import edu.umass.cs.ciir.waltz.dociter.movement.AnyOfMover;
import edu.umass.cs.ciir.waltz.dociter.movement.Mover;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;

import java.util.List;

/**
 * Highlander principle if all the children have the same posting.
 * @author jfoley
 */
public class MergedPostingMover<T> extends AnyOfMover implements PostingMover<T> {
  public MergedPostingMover(List<? extends PostingMover<T>> children) {
    super(children);
  }

  /** Don't have two lists of children just for this function, so use an unsafe cast here. */
  @Override
  @SuppressWarnings("unchecked")
  public T getCurrentPosting() {
    int doc = this.currentKey();
    for (Mover child : children) {
      if(child.matches(doc)) {
        return ((PostingMover<T>) child).getCurrentPosting();
      }
    }
    return null;
  }
}
