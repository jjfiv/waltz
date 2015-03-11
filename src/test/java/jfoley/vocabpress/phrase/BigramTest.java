package jfoley.vocabpress.phrase;

import ciir.jfoley.chai.collections.IntRange;
import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.collections.util.ArrayFns;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class BigramTest {

  @Test
  public void testCount() throws Exception {
    int[] a = { 1, 7, 11, 15, 30, 100};
    int[] b = {    6,     14,     99};
    int[] c = new IntList(IntRange.inclusive(0,100)).asArray();

    assertEquals(0, Bigram.count(a, b));
    assertEquals(3, Bigram.count(b, a));
    assertEquals(a.length, Bigram.count(c, a));
    assertEquals(b.length, Bigram.count(c, b));
  }

  @Test
  public void testPositions() throws Exception {
    int[] a = { 1, 7, 11, 15, 30, 100};
    int[] b = {    6,     14,     99};
    int[] c = new IntList(IntRange.inclusive(0,100)).asArray();

    assertEquals(Collections.<Integer>emptyList(), Bigram.positions(a, b));
    assertEquals(ArrayFns.toList(b), Bigram.positions(b, a));
    assertEquals(Arrays.asList(0,6,10,14,29,99), Bigram.positions(c, a));
    assertEquals(Arrays.asList(5,13,98), Bigram.positions(c, b));
  }
}