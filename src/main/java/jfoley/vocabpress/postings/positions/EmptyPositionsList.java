package jfoley.vocabpress.postings.positions;

import java.util.Collections;
import java.util.List;

/**
 * @author jfoley
 */
public class EmptyPositionsList implements PositionsList {
  public static final PositionsList instance = new EmptyPositionsList();

  /** Private because you should use the singleton instance instead. */
  private EmptyPositionsList() {

  }

  @Override
  public int getPosition(int index) {
    return 0;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public PositionsIterator getExtentsIterator() {
    return new PositionsIterator(this);
  }

  @Override
  public List<Integer> toList() {
    return Collections.emptyList();
  }
}
