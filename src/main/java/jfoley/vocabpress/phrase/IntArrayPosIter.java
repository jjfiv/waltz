package jfoley.vocabpress.phrase;

/**
* @author jfoley
*/
public class IntArrayPosIter implements ExtentsIterator {
  private final int[] data;
  private int pos;
  private final int size;

  public IntArrayPosIter(int[] data) {
    this.data = data;
    this.pos = 0;
    this.size = data.length;
  }

  @Override
  public boolean isDone() {
    return pos >= size;
  }

  @Override
  public boolean next() {
    pos++;
    return !isDone();
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
