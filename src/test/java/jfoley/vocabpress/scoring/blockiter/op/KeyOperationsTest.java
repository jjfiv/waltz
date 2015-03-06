package jfoley.vocabpress.scoring.blockiter.op;

import jfoley.vocabpress.scoring.blockiter.KeyBlock;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class KeyOperationsTest {
  @Test
  public void testIntersection() {
    assertEquals(0, KeyOperations.intersection(KeyBlock.of(1,2,3), KeyBlock.of(4,5)).size());
    assertEquals(Arrays.asList(3), KeyOperations.intersection(KeyBlock.of(1,2,3), KeyBlock.of(3,4,5)));
    assertEquals(Arrays.asList(3,7), KeyOperations.intersection(KeyBlock.of(1,2,3,6,7), KeyBlock.of(3,4,5,7,10)));
  }

}