package jfoley.vocabpress.scoring.blockiter.movement;

import jfoley.vocabpress.scoring.CountPosting;
import jfoley.vocabpress.scoring.blockiter.ListBlockPostingsIterator;
import jfoley.vocabpress.scoring.impl.SimpleCountPosting;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FeatureBlockMoverTest {
	@Test
	public void testBlock() {
		List<CountPosting> data = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			data.add(new SimpleCountPosting(i, i*2));
		}

		FeatureBlockMover<CountPosting> mover = new FeatureBlockMover<>(new ListBlockPostingsIterator<>(data, 3));

		int total = 0;
		for(; !mover.isDone(); mover.nextBlock()) {
			for(; !mover.isDoneWithBlock(); mover.nextKey()) {
				int doc = mover.currentKey();

				total++;
				assertTrue(mover.hasFeature(doc));
				CountPosting count = mover.getFeature(doc);
				assertEquals(doc, count.getKey());
				assertEquals(doc*2, count.getCount());
			}
			assertTrue(mover.isDoneWithBlock());
		}

		assertEquals(100, total);
	}

}