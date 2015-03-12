package jfoley.vocabpress.scoring.blockiter.movement;

import jfoley.vocabpress.dociter.ListBlockPostingsIterator;
import jfoley.vocabpress.dociter.movement.BlockPostingsMover;
import jfoley.vocabpress.postings.CountPosting;
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

		BlockPostingsMover<CountPosting> mover = new BlockPostingsMover<>(new ListBlockPostingsIterator<>(data, 3));

		int total = 0;
		for(; !mover.isDone(); mover.next()) {
			int doc = mover.currentKey();

			total++;
			assertTrue(mover.matches(doc));
			CountPosting count = mover.getCurrentPosting();
			assertEquals(doc, count.getKey());
			assertEquals(doc*2, count.getCount());
		}

		assertEquals(100, total);
	}

}