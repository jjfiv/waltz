package jfoley.vocabpress.phrase;

import ciir.jfoley.chai.collections.IntRange;
import jfoley.vocabpress.postings.positions.PositionsList;
import jfoley.vocabpress.postings.positions.SimplePositionsList;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class BigramTest {

  @Test
  public void testCount() throws Exception {
    PositionsList a = SimplePositionsList.of(1, 7, 11, 15, 30, 100);
    PositionsList b = SimplePositionsList.of(   6,     14,     99);
    PositionsList c = new SimplePositionsList(IntRange.inclusive(0,100));

    assertEquals(0, Bigram.count(a, b));
    assertEquals(3, Bigram.count(b, a));
    assertEquals(a.size(), Bigram.count(c, a));
    assertEquals(b.size(), Bigram.count(c, b));
  }

  @Test
  public void testPositions() throws Exception {
    PositionsList a = SimplePositionsList.of(1, 7, 11, 15, 30, 100);
    PositionsList b = SimplePositionsList.of(   6,     14,     99);
    PositionsList c = new SimplePositionsList(IntRange.inclusive(0,100));

    assertEquals(Collections.<Integer>emptyList(), Bigram.positions(a, b).toList());
    assertEquals(b.toList(), Bigram.positions(b, a).toList());
    assertEquals(Arrays.asList(0, 6, 10, 14, 29, 99), Bigram.positions(c, a).toList());
    assertEquals(Arrays.asList(5, 13, 98),  Bigram.positions(c, b).toList());
  }
}