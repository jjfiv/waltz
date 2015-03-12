package jfoley.vocabpress.extents;

import java.util.AbstractList;

/**
* @author jfoley
*/
public class BeginsView extends AbstractList<Integer> {
  private final ExtentsList extents;

  public BeginsView(ExtentsList extents) {
    this.extents = extents;
  }

  @Override
  public Integer get(int index) {
    return extents.getBegin(index);
  }

  @Override
  public int size() {
    return extents.size();
  }
}
