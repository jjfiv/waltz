package edu.umass.cs.ciir.waltz.phrase;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.postings.extents.SpanIterable;
import edu.umass.cs.ciir.waltz.postings.extents.SpanIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jfoley
 */
public class OrderedWindow {

  /** This is the equivalent of Galago and Indri's od:x(a, b, ...) operator */
  public static int count(List<? extends SpanIterable> positions, int spacingBetween) {
    // TODO, check for and call faster Bigram if applicable :)
    assert(positions.size() >= 2);
    List<SpanIterator> arr = new ArrayList<>(positions.size());
    for (SpanIterable pl : positions) {
      arr.add(pl.getSpanIterator());
    }
    return countIter(arr, spacingBetween);
  }

  public static int countIter(List<? extends SpanIterator> arrayIterators, int width) {
    int hits = 0;
    boolean notDone = true;
    while(notDone) {
      // find the start of the first word
      boolean invalid = false;

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

  public static IntList find(List<? extends SpanIterable> positions, int width) {
    assert(positions.size() >= 2);
    List<SpanIterator> arr = new ArrayList<>(positions.size());
    for (SpanIterable pl : positions) {
      arr.add(pl.getSpanIterator());
    }
    return findIter(arr, width);
  }
  public static IntList findIter(List<? extends SpanIterator> arrayIterators, int width) {
    IntList hits = new IntList();
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
        hits.add(begin);
      }

      // move the first iterator forward - we are double dipping on all other iterators.
      notDone = arrayIterators.get(0).next();
    }

    return hits;
  }

  public static IntList findOr(List<? extends SpanIterator> arrayIterators) {
    IntList hits = new IntList();
    int lastMin = -1;
    while(true) {
      // find minimum
      int min = Integer.MAX_VALUE;
      for (SpanIterator iter : arrayIterators) {
        while (!iter.isDone() && iter.currentBegin() <= lastMin) {
          iter.next();
        }
        if (iter.isDone()) continue;
        int begin = iter.currentBegin();
        if (begin < min) {
          min = begin;
        }
      }
      if(min == Integer.MAX_VALUE) break;

      // add min:
      hits.add(min);
      lastMin = min;
    }

    return hits;
  }



}
