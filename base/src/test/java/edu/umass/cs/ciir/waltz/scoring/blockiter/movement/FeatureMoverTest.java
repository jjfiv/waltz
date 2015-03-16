package edu.umass.cs.ciir.waltz.scoring.blockiter.movement;

import edu.umass.cs.ciir.waltz.dociter.ListBlockPostingsIterator;
import edu.umass.cs.ciir.waltz.dociter.movement.BlockPostingsMover;
import edu.umass.cs.ciir.waltz.postings.Posting;
import edu.umass.cs.ciir.waltz.postings.SimplePosting;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FeatureMoverTest {
	@Test
	public void testBlock() {
		List<Posting<Integer>> data = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			data.add(new SimplePosting<>(i, i*2));
		}

		BlockPostingsMover<Integer> mover = new BlockPostingsMover<>(new ListBlockPostingsIterator<>(data, 3));

		int total = 0;
		for(; !mover.isDone(); mover.next()) {
			int doc = mover.currentKey();

			total++;
			assertTrue(mover.matches(doc));
			Assert.assertEquals(doc * 2, mover.getCurrentPosting().intValue());
		}

		assertEquals(100, total);
	}

}