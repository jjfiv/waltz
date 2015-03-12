package jfoley.vocabpress.index.mem;

import ciir.jfoley.chai.collections.list.IntList;
import jfoley.vocabpress.dociter.movement.PostingMover;
import jfoley.vocabpress.feature.Feature;
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

    PostingMover<CountPosting> foxCounts = index.getCountsMover("fox");

		List<Integer> foxHits = new IntList();
    for(; !foxCounts.isDone(); foxCounts.next()) {
      int doc = foxCounts.currentKey();
      assertTrue(foxCounts.matches(doc));
      CountPosting p = foxCounts.getCurrentPosting();
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

    PostingMover<PositionsPosting> foxIter = index.getPositionsMover("fox");

    IntList foxPos = new IntList();
    List<Integer> foxHits = new IntList();
    for(; !foxIter.isDone(); foxIter.next()) {
      int doc = foxIter.currentKey();
      assertTrue(foxIter.matches(doc));
      PositionsPosting p = foxIter.getCurrentPosting();
      //System.err.println(p);
      foxPos.addAll(p.getPositions().toList());
      foxHits.add(p.getCount());
      assertEquals(doc, p.getKey());
    }
    assertEquals(Arrays.asList(1,1), foxHits);
    assertEquals(Arrays.asList(1,3), foxPos);

    assertEquals(Arrays.asList(3), index.getPositions("fox").getFeature(1).getPositions().toList());
    assertEquals(Arrays.asList(8), index.getPositions("dog").getFeature(1).getPositions().toList());
  }

  @Test
  public void testAllDocuments() throws Exception {
    MemoryPositionsIndex index = new MemoryPositionsIndex();
    index.addDocument("fox", tokens("a fox is a mammal"));
    index.addDocument("quick", tokens("the quick brown fox jumped over the lazy dog"));

    Feature<? extends CountPosting> foxCounts = index.getCounts("fox");

    List<Integer> foxHits = new IntList();

    for (int docId : index.getAllDocumentIds()) {
      if(foxCounts.hasFeature(docId)) {
        CountPosting p = foxCounts.getFeature(docId);
        foxHits.add(p.getCount());
      }
    }

    assertEquals(Arrays.asList(1,1), foxHits);
  }
}