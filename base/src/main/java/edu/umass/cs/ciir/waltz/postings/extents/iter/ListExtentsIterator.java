package edu.umass.cs.ciir.waltz.postings.extents.iter;

import edu.umass.cs.ciir.waltz.postings.extents.ExtentsList;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
* @author jfoley
*/
public class ListExtentsIterator extends AExtentsIterator {
  private final ExtentsList extents;

  public ListExtentsIterator(@Nonnull ExtentsList extents) {
    super(extents.size());
    this.extents = Objects.requireNonNull(extents);
  }

  @Override
  public int currentBegin() {
    return extents.get(pos).begin;
  }

  @Override
  public int currentEnd() {
    return extents.get(pos).end;
  }
}
