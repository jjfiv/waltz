package jfoley.vocabpress.phrase;

import ciir.jfoley.chai.collections.IntRange;
import ciir.jfoley.chai.collections.list.IntList;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class OrderedWindowTest {

  @Test
  public void testOd1() throws Exception {
    int[] a = { 1, 7, 11, 15, 30, 100};
    int[] b = {    6,     14,     99};
    int[] c = new IntList(IntRange.inclusive(0, 100)).asArray();

    assertEquals(0, OrderedWindow.countPositions(Arrays.asList(a, b), 1));
    assertEquals(3, OrderedWindow.countPositions(Arrays.asList(b, a), 1));
    assertEquals(a.length, OrderedWindow.countPositions(Arrays.asList(c, a), 1));
    assertEquals(b.length, OrderedWindow.countPositions(Arrays.asList(c, b), 1));
  }
}