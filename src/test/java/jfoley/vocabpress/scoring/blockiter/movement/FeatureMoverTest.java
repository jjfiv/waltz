package jfoley.vocabpress.scoring.blockiter.movement;

import jfoley.vocabpress.scoring.CountPosting;
import jfoley.vocabpress.scoring.blockiter.ListBlockPostingsIterator;
import jfoley.vocabpress.scoring.impl.SimpleCountPosting;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class FeatureMoverTest {

  @Test
  public void testByHand() throws Exception {
    List<CountPosting> exampleData = new ArrayList<>();
    for (int i = 0; i < 35; i++) {
      exampleData.add(new SimpleCountPosting(i, i*2));
    }

    ListBlockPostingsIterator<CountPosting> countBlocks = new ListBlockPostingsIterator<>(exampleData, 16);

    FeatureMover<CountPosting> featureMover = new FeatureMover<>(countBlocks);
    int total = 0;
    for(; !featureMover.isDone(); featureMover.next()) {
      int doc = featureMover.currentKey();
      total++;

      assertTrue(featureMover.hasFeature(doc));
      CountPosting count = featureMover.getFeature(doc);
      assertEquals(doc, count.getKey());
      assertEquals(doc * 2, count.getCount());
    }
    assertEquals(35, total);
  }
}