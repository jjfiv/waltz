package jfoley.vocabpress.scoring.blockiter.movement;

import jfoley.vocabpress.scoring.CountPosting;
import jfoley.vocabpress.scoring.blockiter.BlockPostingsIterator;
import jfoley.vocabpress.scoring.blockiter.ListBlockPostingsIterator;
import jfoley.vocabpress.scoring.impl.SimpleCountPosting;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class AnyOfMoverTest {

  @Test
  public void testNext() throws Exception {
    BlockPostingsIterator<CountPosting> lhsData = new ListBlockPostingsIterator<>(Arrays.asList(
        new SimpleCountPosting(1,1),
        new SimpleCountPosting(3,3),
        new SimpleCountPosting(5,5),
        new SimpleCountPosting(7,7)
    ), 3);
    BlockPostingsIterator<CountPosting> rhsData = new ListBlockPostingsIterator<>(Arrays.asList(
        new SimpleCountPosting(0,0),
        new SimpleCountPosting(2,4),
        new SimpleCountPosting(5,10),
        new SimpleCountPosting(8,16)
    ), 3);

    FeatureMover<CountPosting> lhs = new FeatureMover<>(lhsData);
    FeatureMover<CountPosting> rhs = new FeatureMover<>(rhsData);
    AnyOfMover mover = new AnyOfMover(Arrays.asList(lhs, rhs));

    int total = 0;
    for(; !mover.isDone(); mover.next()) {
      int doc = mover.currentKey();
      total++;

      if(lhs.hasFeature(doc)) {
        CountPosting lcount = lhs.getFeature(doc);
        assertEquals(doc, lcount.getKey());
        assertEquals(doc, lcount.getCount());
      }
      if(rhs.hasFeature(doc)) {
        CountPosting rcount = rhs.getFeature(doc);
        assertEquals(doc, rcount.getKey());
        assertEquals(doc*2, rcount.getCount());
      }
      if(lhs.hasFeature(doc) && rhs.hasFeature(doc)) {
        assertEquals(5, doc);
      }
    }
    assertEquals(7, total);
  }
}