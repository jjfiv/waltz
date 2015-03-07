package jfoley.vocabpress.scoring.blockiter.movement;

import jfoley.vocabpress.scoring.CountPosting;
import jfoley.vocabpress.scoring.blockiter.BlockPostingsIterator;
import jfoley.vocabpress.scoring.blockiter.ListBlockPostingsIterator;
import jfoley.vocabpress.scoring.impl.SimpleCountPosting;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AnyOfBlockMoverTest {

	@Test
	public void testIsDone() throws Exception {
		BlockPostingsIterator<CountPosting> lhsData = new ListBlockPostingsIterator<>(Arrays.asList(
			new SimpleCountPosting(1, 1),
			new SimpleCountPosting(3, 3),
			new SimpleCountPosting(5, 5),
			new SimpleCountPosting(7, 7),
			new SimpleCountPosting(9, 9)
		), 2);
		BlockPostingsIterator<CountPosting> rhsData = new ListBlockPostingsIterator<>(Arrays.asList(
			new SimpleCountPosting(0,0),
			new SimpleCountPosting(2,4),
			new SimpleCountPosting(5,10),
			new SimpleCountPosting(8,16)
		), 3);

		FeatureBlockMover<CountPosting> lhs = new FeatureBlockMover<>(lhsData);
		FeatureBlockMover<CountPosting> rhs = new FeatureBlockMover<>(rhsData);
		AnyOfBlockMover mover = new AnyOfBlockMover(Arrays.asList(lhs, rhs));

		List<Integer> hits = new ArrayList<Integer>();
		int total = 0;
		for(; !mover.isDone(); mover.nextBlock()) {
			for(; !mover.isDoneWithBlock(); mover.nextKey()) {
				int doc = mover.currentKey();
				System.out.println(doc);

				total++;
				hits.add(doc);

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
		}

		assertEquals(Arrays.asList(0,1,2,3,5,7,8,9), hits);
		assertTrue(lhs.isDoneWithBlock());
		assertTrue(rhs.isDoneWithBlock());
		assertTrue(rhs.isDone());
		assertTrue(lhs.isDone());
		assertEquals(8, total);
	}
}