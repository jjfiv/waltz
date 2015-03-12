package jfoley.vocabpress.postings.extents.iter;

/**
* @author jfoley
*/
public class IntArrayPosIter extends AExtentsIterator {
  private final int[] data;

  public IntArrayPosIter(int[] data, int size) {
    super(size);
    this.data = data;
  }
  public IntArrayPosIter(int[] data) {
    this(data, data.length);
  }

  @Override
  public int currentBegin() {
    return data[pos];
  }

  @Override
  public int currentEnd() {
    return data[pos]+1;
  }
}
