package jfoley.vocabpress.phrase;

import jfoley.vocabpress.extents.ExtentsIterator;
import jfoley.vocabpress.extents.IntArrayPosIter;
import jfoley.vocabpress.scoring.Extent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class UnorderedWindow {
  /** This is the equivalent of Galago and Indri's uw:x(a, b, ...) operator */
  public static int countPositions(List<int[]> positions, int totalSpacing) {
    // TODO, check for and call faster Bigram if applicable :)

    assert(positions.size() >= 2);
    List<ExtentsIterator> arr = new ArrayList<>(positions.size());
    for (int[] position : positions) {
      arr.add(new IntArrayPosIter(position));
    }
    return countExtents(arr, totalSpacing);
  }

  public static int countExtents(List<ExtentsIterator> iters, int width) {
    int hits = 0;

    int max = iters.get(0).currentEnd();
    int min = iters.get(0).currentBegin();
    for (int i = 1; i < iters.size(); i++) {
      max = Math.max(max, iters.get(i).currentEnd());
      min = Math.min(min, iters.get(i).currentBegin());
    }

    while(true) {
      boolean match = (max - min <= width) || (width == -1);
      if (match) {
        hits++;
      }

      int oldMin = min;
      // now, reset bounds
      max = Integer.MIN_VALUE;
      min = Integer.MAX_VALUE;
      for (ExtentsIterator iter : iters) {
        if (iter.currentBegin() == oldMin) {
          boolean notDone = iter.next();
          if (!notDone) {
            return hits;
          }
          assert(iter.currentBegin() > oldMin);
        }
        max = Math.max(max, iter.currentEnd());
        min = Math.min(min, iter.currentBegin());
      }
    }
  }

  public static List<Extent> calculateExtents(List<ExtentsIterator> iters, int width) {
    List<Extent> hits = new ArrayList<>();

    int max = iters.get(0).currentEnd();
    int min = iters.get(0).currentBegin();
    for (int i = 1; i < iters.size(); i++) {
      max = Math.max(max, iters.get(i).currentEnd());
      min = Math.min(min, iters.get(i).currentBegin());
    }

    while(true) {
      boolean match = (max - min <= width) || (width == -1);
      if (match) {
        hits.add(new Extent(min, max));
      }

      int oldMin = min;
      // now, reset bounds
      max = Integer.MIN_VALUE;
      min = Integer.MAX_VALUE;
      for (ExtentsIterator iter : iters) {
        if (iter.currentBegin() == oldMin) {
          boolean notDone = iter.next();
          if (!notDone) {
            return hits;
          }
          assert(iter.currentBegin() > oldMin);
        }
        max = Math.max(max, iter.currentEnd());
        min = Math.min(min, iter.currentBegin());
      }
    }
  }

}
