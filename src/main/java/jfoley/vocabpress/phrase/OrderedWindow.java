package jfoley.vocabpress.phrase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class OrderedWindow {

  public static interface ExtentsIterator {
    /** returns true if there are no more elements to step over */
    public boolean isDone();
    /** returns !isDone() after moving once */
    public boolean next();
    /** return the beginning of this extent */
    public int currentBegin();
    /** return the end of this extent -- if it's a position, this is always currentBegin()+1 */
    public int currentEnd();
  }

  public static class IntArrayPosIter implements ExtentsIterator {
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

  /** This is the equivalent of Galago and Indri's od:x(a, b, ...) operator */
  public static int countPositions(List<int[]> positions, int spacingBetween) {
    assert(positions.size() >= 2);
    List<ExtentsIterator> arr = new ArrayList<>(positions.size());
    for (int[] position : positions) {
      arr.add(new IntArrayPosIter(position));
    }
    return countExtents(arr, spacingBetween);
  }

  public static int countExtents(List<ExtentsIterator> arrayIterators, int width) {
    int hits = 0;
    boolean notDone = true;
    while(notDone) {
      // find the start of the first word
      boolean invalid = false;
      int begin = arrayIterators.get(0).currentBegin();

      // loop over all the rest of the words
      for (int i = 1; i < arrayIterators.size(); i++) {
        int end = arrayIterators.get(i - 1).currentEnd();

        // try to move this iterator so that it's past the end of the previous word
        assert (arrayIterators.get(i) != null);
        assert (!arrayIterators.get(i).isDone());
        while (end > arrayIterators.get(i).currentBegin()) {
          notDone = arrayIterators.get(i).next();

          // if there are no more occurrences of this word,
          // no more ordered windows are possible
          if (!notDone) {
            return hits;
          }
        }

        if (arrayIterators.get(i).currentBegin() - end >= width) {
          invalid = true;
          break;
        }
      }

      // if it's a match, record it
      if (!invalid) {
        hits++;
      }

      // move the first iterator forward - we are double dipping on all other iterators.
      notDone = arrayIterators.get(0).next();
    }

    return hits;
  }
}
