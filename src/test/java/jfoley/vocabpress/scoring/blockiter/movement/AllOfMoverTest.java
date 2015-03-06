package jfoley.vocabpress.scoring.blockiter.movement;

import jfoley.vocabpress.scoring.CountPosting;
import jfoley.vocabpress.scoring.blockiter.BlockPostingsIterator;
import jfoley.vocabpress.scoring.blockiter.ListBlockPostingsIterator;
import jfoley.vocabpress.scoring.impl.SimpleCountPosting;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AllOfMoverTest {

  @Test
  public void testSingleHit() throws Exception {
    BlockPostingsIterator<CountPosting> lhsData = new ListBlockPostingsIterator<>(Arrays.asList(
        new SimpleCountPosting(1, 1),
        new SimpleCountPosting(3, 3),
        new SimpleCountPosting(5, 5),
        new SimpleCountPosting(7, 7),
        new SimpleCountPosting(107, 7),
        new SimpleCountPosting(117, 7)
    ), 1);
    BlockPostingsIterator<CountPosting> rhsData = new ListBlockPostingsIterator<>(Arrays.asList(
        new SimpleCountPosting(0,0),
        new SimpleCountPosting(2,4),
        new SimpleCountPosting(5,10),
        new SimpleCountPosting(8,16)
    ), 3);

    FeatureMover<CountPosting> lhs = new FeatureMover<>(lhsData);
    FeatureMover<CountPosting> rhs = new FeatureMover<>(rhsData);
    AllOfMover mover = new AllOfMover(Arrays.asList(lhs, rhs));

    int total = 0;
    for(; !mover.isDone(); mover.next()) {
      int doc = mover.currentKey();
      total++;

      if(lhs.hasFeature(doc)) {
        assertTrue(rhs.hasFeature(doc));

        CountPosting lcount = lhs.getFeature(doc);
        assertEquals(doc, lcount.getKey());
        assertEquals(doc, lcount.getCount());

        CountPosting rcount = rhs.getFeature(doc);
        assertEquals(doc, rcount.getKey());
        assertEquals(doc*2, rcount.getCount());

        assertEquals(5, doc);
      }
    }
    assertEquals(1, total);
  }
}