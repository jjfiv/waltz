package jfoley.vocabpress.phrase;

import jfoley.vocabpress.extents.IntArrayPosIter;
import jfoley.vocabpress.scoring.Extent;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class UnorderedWindowTest {

  @Test
  public void testCountPositions() throws Exception {
    int[] a = { 1, 7, 11, 15, 30, 100};
    int[] b = {    6,     14,     99};

    assertEquals(4, UnorderedWindow.countPositions(Arrays.asList(a, b), 4));
    assertEquals(Arrays.asList(
        Extent.of(6, 8),
        Extent.of(11, 15),
        Extent.of(14, 16),
        Extent.of(99, 101)
        ), UnorderedWindow.calculateExtents(Arrays.asList(new IntArrayPosIter(a), new IntArrayPosIter(b)), 4));
  }
}