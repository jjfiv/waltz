package jfoley.vocabpress.io.codec;

import ciir.jfoley.chai.collections.list.IntList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ListCoderTest {

  @Test
  public void testKnownStrings() throws Exception {
    ListCoder<String> strs = new ListCoder<>(UTF8.withVByteLength);
    assertTrue(strs.knowsOwnSize());

    List<String> testData = new ArrayList<>();
    testData.add("Hello World!");
    testData.add("Hello Variable Variable Length!");
    testData.add("Fox!");
    testData.add("Alpha!");
    testData.add("Bravo");

    assertEquals(testData, strs.read(strs.write(testData)));
  }

  @Test
  public void testRandomNums() throws Exception {
    ListCoder<Integer> strs = new ListCoder<>(VByteCoders.ints);
    assertTrue(strs.knowsOwnSize());

    IntList testData = new IntList();
    Random r = new Random();

    for (int i = 0; i < 1000; i++) {
      testData.add(Math.abs(r.nextInt()));
    }

    assertEquals(testData, strs.read(strs.write(testData)));
  }
}