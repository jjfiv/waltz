package jfoley.vocabpress.mem;

import jfoley.vocabpress.feature.Feature;
import jfoley.vocabpress.movement.Mover;
import jfoley.vocabpress.scoring.CountPosting;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MemoryPositionsIndexTest {

  static List<String> tokens(String input) {
    return Arrays.asList(input.toLowerCase().split("\\s+"));
  }

  @Test
  public void testGetCounts() throws Exception {
    MemoryPositionsIndex index = new MemoryPositionsIndex();
    index.addDocument("fox", tokens("a fox is a mammal"));
    index.addDocument("quick", tokens("the quick brown fox jumped over the lazy dog"));

    Feature<? extends CountPosting> foxCounts = index.getCounts("fox");

    for(Mover mover = foxCounts.getMover(); !mover.isDone(); mover.next()) {
      int doc = mover.currentKey();
      assertTrue(foxCounts.hasFeature(doc));
      CountPosting p = foxCounts.getFeature(doc);
      assertEquals(doc, p.getKey());
      assertEquals(1, p.getCount());
    }
  }
}