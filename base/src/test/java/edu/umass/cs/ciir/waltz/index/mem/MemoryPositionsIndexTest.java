package edu.umass.cs.ciir.waltz.index.mem;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.feature.Feature;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import org.junit.Assert;
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

    PostingMover<Integer> foxCounts = index.getCountsMover("fox");

		List<Integer> foxHits = new IntList();
    for(; !foxCounts.isDone(); foxCounts.next()) {
      int doc = foxCounts.currentKey();
      assertTrue(foxCounts.matches(doc));
			foxHits.add(foxCounts.getCurrentPosting());
    }
		assertEquals(Arrays.asList(1,1), foxHits);
  }

  @Test
  public void testPositions() {
    MemoryPositionsIndex index = new MemoryPositionsIndex();
    index.addDocument("fox", tokens("a fox is a mammal"));
    index.addDocument("quick", tokens("the quick brown fox jumped over the lazy dog"));

    PostingMover<PositionsList> foxIter = index.getPositionsMover("fox");

    IntList foxPos = new IntList();
    List<Integer> foxHits = new IntList();
    for(; !foxIter.isDone(); foxIter.next()) {
      int doc = foxIter.currentKey();
      assertTrue(foxIter.matches(doc));
      PositionsList p = foxIter.getCurrentPosting();
      //System.err.println(p);
      foxPos.addAll(p);
      foxHits.add(p.size());
    }
    assertEquals(Arrays.asList(1,1), foxHits);
    assertEquals(Arrays.asList(1,3), foxPos);

    Assert.assertEquals(Arrays.asList(3), index.getPositions("fox").getFeature(1));
    Assert.assertEquals(Arrays.asList(8), index.getPositions("dog").getFeature(1));
  }

  @Test
  public void testAllDocuments() throws Exception {
    MemoryPositionsIndex index = new MemoryPositionsIndex();
    index.addDocument("fox", tokens("a fox is a mammal"));
    index.addDocument("quick", tokens("the quick brown fox jumped over the lazy dog"));

    Feature<Integer> foxCounts = index.getCounts("fox");

    List<Integer> foxHits = new IntList();

    for (int docId : index.getAllDocumentIds()) {
      if(foxCounts.hasFeature(docId)) {
        foxHits.add(foxCounts.getFeature(docId));
      }
    }

    assertEquals(Arrays.asList(1,1), foxHits);
  }
}