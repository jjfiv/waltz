package edu.umass.cs.ciir.waltz.phrase;

import ciir.jfoley.chai.collections.IntRange;
import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.postings.positions.SimplePositionsList;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OrderedWindowTest {

  @Test
  public void testOd1() throws Exception {
    PositionsList a = SimplePositionsList.of(1, 7, 11, 15, 30, 100);
    PositionsList b = SimplePositionsList.of(   6,     14,     99);
    PositionsList c = new SimplePositionsList(IntRange.inclusive(0, 100));

    assertEquals(0, OrderedWindow.count(Arrays.asList(a, b), 1));
    assertEquals(3, OrderedWindow.count(Arrays.asList(b, a), 1));
    assertEquals(a.size(), OrderedWindow.count(Arrays.asList(c, a), 1));
    assertEquals(b.size(), OrderedWindow.count(Arrays.asList(c, b), 1));
  }

  @Test
  public void testOd1_b() throws Exception {
    PositionsList a = SimplePositionsList.of(1, 7, 11, 15, 30, 100);
    PositionsList b = SimplePositionsList.of(   6,     14,     99);
    PositionsList c = new SimplePositionsList(IntRange.inclusive(0, 100));

    assertEquals(0, OrderedWindow.find(Arrays.asList(a, b), 1).size());
    assertEquals(3, OrderedWindow.find(Arrays.asList(b, a), 1).size());
    assertEquals(new IntList(Arrays.asList(6, 14, 99)), OrderedWindow.find(Arrays.asList(b, a), 1));
    assertEquals(a.size(), OrderedWindow.find(Arrays.asList(c, a), 1).size());
    assertEquals(b.size(), OrderedWindow.find(Arrays.asList(c, b), 1).size());
  }

  @Test
  public void testOr() throws Exception {
    PositionsList a = SimplePositionsList.of(1, 7, 11, 15, 30, 100);
    PositionsList b = SimplePositionsList.of(   6,     14,     99);
    PositionsList c = new SimplePositionsList(IntRange.inclusive(0, 100));

    assertEquals((List) Arrays.asList(1,6,7,11,14,15,30,99,100),
        OrderedWindow.findOr(Arrays.asList(a.getSpanIterator(), b.getSpanIterator())));
  }

}