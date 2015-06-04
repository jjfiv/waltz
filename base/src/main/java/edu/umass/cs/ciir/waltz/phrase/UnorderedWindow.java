package edu.umass.cs.ciir.waltz.phrase;

import edu.umass.cs.ciir.waltz.postings.extents.InterleavedSpans;
import edu.umass.cs.ciir.waltz.postings.extents.SpanIterable;
import edu.umass.cs.ciir.waltz.postings.extents.Span;
import edu.umass.cs.ciir.waltz.postings.extents.SpanIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class UnorderedWindow {
  /** This is the equivalent of Galago and Indri's uw:x(a, b, ...) operator */
  public static int countPositions(List<? extends SpanIterable> positions, int totalSpacing) {
    // TODO, check for and call faster Bigram if applicable :)

    assert(positions.size() >= 2);
    List<SpanIterator> arr = new ArrayList<>(positions.size());
    for (SpanIterable position : positions) {
      arr.add(position.getSpanIterator());
    }
    return countSpans(arr, totalSpacing);
  }

  public static int countSpans(List<SpanIterator> iters, int width) {
    int hits = 0;

    int max = iters.get(0).currentEnd();
    int min = iters.get(0).currentBegin();
    for (int i = 1; i < iters.size(); i++) {
      max = Math.max(max, iters.get(i).currentEnd());
      min = Math.min(min, iters.get(i).currentBegin());
    }

    while(true) {
      //System.err.println(Arrays.asList(min, max));
      boolean match = (max - min <= width) || (width == -1);
      if (match) {
        hits++;
      }

      int oldMin = min;
      // now, reset bounds
      max = Integer.MIN_VALUE;
      min = Integer.MAX_VALUE;
      for (SpanIterator iter : iters) {
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

  public static List<Span> calculateSpans(List<SpanIterator> iters, int width) {
    InterleavedSpans hits = new InterleavedSpans();

    int max = iters.get(0).currentEnd();
    int min = iters.get(0).currentBegin();
    for (int i = 1; i < iters.size(); i++) {
      max = Math.max(max, iters.get(i).currentEnd());
      min = Math.min(min, iters.get(i).currentBegin());
    }

    while(true) {
      boolean match = (max - min <= width) || (width == -1);
      if (match) {
        hits.push(min, max);
      }

      int oldMin = min;
      // now, reset bounds
      max = Integer.MIN_VALUE;
      min = Integer.MAX_VALUE;
      for (SpanIterator iter : iters) {
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
