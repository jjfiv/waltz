package edu.umass.cs.ciir.waltz.dociter;

import edu.umass.cs.ciir.waltz.dociter.movement.AnyOfMover;
import edu.umass.cs.ciir.waltz.dociter.movement.Mover;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.statistics.PostingListStatistics;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Highlander principle if all the children have the same posting.
 * @author jfoley
 */
public class MergedPostingMover<T, M extends PostingMover<T>> extends AnyOfMover<M> implements PostingMover<T> {
  public MergedPostingMover(List<M> children) {
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

  @Nullable
  @Override
  public PostingListStatistics<T> getStatistics() {
    PostingListStatistics<T> firstChildStats = children.get(0).getStatistics();
    if(firstChildStats == null) return null;
    PostingListStatistics<T> result = firstChildStats.copy();
    for (int i = 1; i < children.size(); i++) {
      PostingListStatistics<T> nthChildStats = children.get(i).getStatistics();
      if(nthChildStats == null) return null;
      result.add(nthChildStats);
    }
    return result;
  }
}
