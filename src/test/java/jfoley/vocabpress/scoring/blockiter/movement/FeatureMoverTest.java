package jfoley.vocabpress.scoring.blockiter.movement;

import jfoley.vocabpress.feature.FeatureMover;
import jfoley.vocabpress.postings.CountPosting;
import jfoley.vocabpress.dociter.ListBlockPostingsIterator;
import jfoley.vocabpress.postings.impl.SimpleCountPosting;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FeatureMoverTest {
	@Test
	public void testBlock() {
		List<CountPosting> data = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			data.add(new SimpleCountPosting(i, i*2));
		}

		FeatureMover<CountPosting> mover = new FeatureMover<>(new ListBlockPostingsIterator<>(data, 3));

		int total = 0;
		for(; !mover.isDone(); mover.next()) {
			int doc = mover.currentKey();

			total++;
			assertTrue(mover.hasFeature(doc));
			CountPosting count = mover.getFeature(doc);
			assertEquals(doc, count.getKey());
			assertEquals(doc*2, count.getCount());
		}

		assertEquals(100, total);
	}

}