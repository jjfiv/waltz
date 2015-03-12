package jfoley.vocabpress.phrase;

import ciir.jfoley.chai.collections.IntRange;
import jfoley.vocabpress.positions.PositionsList;
import jfoley.vocabpress.positions.SimplePositionsList;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class OrderedWindowTest {

  @Test
  public void testOd1() throws Exception {
    PositionsList a = SimplePositionsList.of(1, 7, 11, 15, 30, 100);
    PositionsList b = SimplePositionsList.of(   6,     14,     99);
    PositionsList c = new SimplePositionsList(IntRange.inclusive(0, 100));

    assertEquals(0, OrderedWindow.countPositions(Arrays.asList(a, b), 1));
    assertEquals(3, OrderedWindow.countPositions(Arrays.asList(b, a), 1));
    assertEquals(a.size(), OrderedWindow.countPositions(Arrays.asList(c, a), 1));
    assertEquals(b.size(), OrderedWindow.countPositions(Arrays.asList(c, b), 1));
  }
}