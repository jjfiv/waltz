package edu.umass.cs.ciir.waltz.phrase;

import ciir.jfoley.chai.collections.IntRange;
import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.postings.positions.SimplePositionsList;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class OrderedWindowTest {

  @Test
  public void testOd1() throws Exception {
    PositionsList a = SimplePositionsList.of(1, 7, 11, 15, 30, 100);
    PositionsList b = SimplePositionsList.of(   6,     14,     99);
    PositionsList c = new SimplePositionsList(IntRange.inclusive(0, 100));

    Assert.assertEquals(0, OrderedWindow.count(Arrays.asList(a, b), 1));
    Assert.assertEquals(3, OrderedWindow.count(Arrays.asList(b, a), 1));
    Assert.assertEquals(a.size(), OrderedWindow.count(Arrays.asList(c, a), 1));
    Assert.assertEquals(b.size(), OrderedWindow.count(Arrays.asList(c, b), 1));
  }

  @Test
  public void testOd1_b() throws Exception {
    PositionsList a = SimplePositionsList.of(1, 7, 11, 15, 30, 100);
    PositionsList b = SimplePositionsList.of(   6,     14,     99);
    PositionsList c = new SimplePositionsList(IntRange.inclusive(0, 100));

    Assert.assertEquals(0, OrderedWindow.find(Arrays.asList(a, b), 1).size());
    Assert.assertEquals(3, OrderedWindow.find(Arrays.asList(b, a), 1).size());
    Assert.assertEquals(new IntList(Arrays.asList(6, 14, 99)), OrderedWindow.find(Arrays.asList(b, a), 1));
    Assert.assertEquals(a.size(), OrderedWindow.find(Arrays.asList(c, a), 1).size());
    Assert.assertEquals(b.size(), OrderedWindow.find(Arrays.asList(c, b), 1).size());
  }

}