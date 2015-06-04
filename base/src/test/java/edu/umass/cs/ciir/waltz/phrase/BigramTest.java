package edu.umass.cs.ciir.waltz.phrase;

import ciir.jfoley.chai.collections.IntRange;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.postings.positions.SimplePositionsList;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BigramTest {

  @Test
  public void testCount() throws Exception {
    PositionsList a = SimplePositionsList.of(1, 7, 11, 15, 30, 100);
    PositionsList b = SimplePositionsList.of(   6,     14,     99);
    PositionsList c = new SimplePositionsList(IntRange.inclusive(0,100));

    Assert.assertEquals(0, Bigram.count(a, b));
    Assert.assertEquals(3, Bigram.count(b, a));
    Assert.assertEquals(a.size(), Bigram.count(c, a));
    Assert.assertEquals(b.size(), Bigram.count(c, b));
  }

  @Test
  public void testPositions() throws Exception {
    PositionsList a = SimplePositionsList.of(1, 7, 11, 15, 30, 100);
    PositionsList b = SimplePositionsList.of(   6,     14,     99);
    PositionsList c = new SimplePositionsList(IntRange.inclusive(0,100));

    assertEquals(Collections.<Integer>emptyList(), Bigram.positions(a, b));
    assertEquals(b, Bigram.positions(b, a));
    assertEquals(Arrays.asList(0, 6, 10, 14, 29, 99), Bigram.positions(c, a));
    assertEquals(Arrays.asList(5, 13, 98), Bigram.positions(c, b));
  }

  void assertEquals(List<Integer> expected, List<Integer> actual) {
    Assert.assertEquals(expected, actual);
  }
}