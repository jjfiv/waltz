package jfoley.vocabpress.scoring.list;

import jfoley.vocabpress.scoring.CountPosting;
import jfoley.vocabpress.scoring.impl.SimpleCountPosting;
import jfoley.vocabpress.scoring.iter.CountIterator;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SimpleCountsPostingListTest {

  @Test
  public void testGetCountIterator() throws Exception {
    SimpleCountsPostingList pl = new SimpleCountsPostingList(Arrays.asList(
        new SimpleCountPosting(1, 10),
        new SimpleCountPosting(2, 20),
        new SimpleCountPosting(100,7),
        new SimpleCountPosting(134, 3)
    ));

    Map<Integer, Integer> postings = new HashMap<>();

    CountIterator iterator = pl.getCountIterator();
    while(iterator.hasCurrent()) {
      CountPosting count = iterator.getCount(iterator.currentKey());
      postings.put(count.getKey(), count.getCount());
      iterator.movePast(count.getKey());
    }

    assertEquals(10, postings.get(1).intValue());
    assertEquals(20, postings.get(2).intValue());
    assertEquals(7, postings.get(100).intValue());
    assertEquals(3, postings.get(134).intValue());
    assertEquals(4, postings.size());
  }
}