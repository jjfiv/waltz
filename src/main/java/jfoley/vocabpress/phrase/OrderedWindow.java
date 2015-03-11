package jfoley.vocabpress.phrase;

import jfoley.vocabpress.extents.ExtentsIterator;
import jfoley.vocabpress.extents.IntArrayPosIter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class OrderedWindow {

  /** This is the equivalent of Galago and Indri's od:x(a, b, ...) operator */
  public static int countPositions(List<int[]> positions, int spacingBetween) {
    // TODO, check for and call faster Bigram if applicable :)

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
