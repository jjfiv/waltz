package edu.umass.cs.ciir.waltz.phrase;

import edu.umass.cs.ciir.waltz.postings.extents.Extent;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.postings.positions.SimplePositionsList;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class UnorderedWindowTest {

  @Test
  public void testCountPositions() throws Exception {
    PositionsList a = SimplePositionsList.of(1, 7, 11, 15, 30, 100);
    PositionsList b = SimplePositionsList.of(    6,     14,     99);

    Assert.assertEquals(4, UnorderedWindow.countPositions(Arrays.asList(a, b), 4));
    Assert.assertEquals(Arrays.asList(
            Extent.of(6, 8),
            Extent.of(11, 15),
            Extent.of(14, 16),
            Extent.of(99, 101)
        ),
        UnorderedWindow.calculateExtents(
            Arrays.asList(a.getExtentsIterator(), b.getExtentsIterator()), 4));
  }
}