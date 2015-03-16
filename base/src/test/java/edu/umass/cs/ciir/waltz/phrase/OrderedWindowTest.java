package edu.umass.cs.ciir.waltz.phrase;

import ciir.jfoley.chai.collections.IntRange;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.postings.positions.SimplePositionsList;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class OrderedWindowTest {

  @Test
  public void testOd1() throws Exception {
    PositionsList a = SimplePositionsList.of(1, 7, 11, 15, 30, 100);
    PositionsList b = SimplePositionsList.of(   6,     14,     99);
    PositionsList c = new SimplePositionsList(IntRange.inclusive(0, 100));

    Assert.assertEquals(0, OrderedWindow.countPositions(Arrays.asList(a, b), 1));
    Assert.assertEquals(3, OrderedWindow.countPositions(Arrays.asList(b, a), 1));
    Assert.assertEquals(a.size(), OrderedWindow.countPositions(Arrays.asList(c, a), 1));
    Assert.assertEquals(b.size(), OrderedWindow.countPositions(Arrays.asList(c, b), 1));
  }
}