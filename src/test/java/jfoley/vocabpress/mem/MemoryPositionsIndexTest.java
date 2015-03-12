package jfoley.vocabpress.mem;

import ciir.jfoley.chai.collections.list.IntList;
import jfoley.vocabpress.feature.Feature;
import jfoley.vocabpress.dociter.movement.Mover;
import jfoley.vocabpress.postings.CountPosting;
import jfoley.vocabpress.postings.positions.PositionsPosting;
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

		List<Integer> foxHits = new IntList();
    for(Mover mover = foxCounts.getMover(); !mover.isDone(); mover.next()) {
      int doc = mover.currentKey();
      assertTrue(foxCounts.hasFeature(doc));
      CountPosting p = foxCounts.getFeature(doc);
			foxHits.add(p.getCount());
      assertEquals(doc, p.getKey());
    }
		assertEquals(Arrays.asList(1,1), foxHits);
  }

  @Test
  public void testPositions() {
    MemoryPositionsIndex index = new MemoryPositionsIndex();
    index.addDocument("fox", tokens("a fox is a mammal"));
    index.addDocument("quick", tokens("the quick brown fox jumped over the lazy dog"));

    Feature<? extends PositionsPosting> foxIter = index.getPositions("fox");

    IntList foxPos = new IntList();
    List<Integer> foxHits = new IntList();
    for(Mover mover = foxIter.getMover(); !mover.isDone(); mover.next()) {
      int doc = mover.currentKey();
      assertTrue(foxIter.hasFeature(doc));
      PositionsPosting p = foxIter.getFeature(doc);
      //System.err.println(p);
      foxPos.addAll(p.getPositions().toList());
      foxHits.add(p.getCount());
      assertEquals(doc, p.getKey());
    }
    assertEquals(Arrays.asList(1,1), foxHits);
    assertEquals(Arrays.asList(1,3), foxPos);
  }
}