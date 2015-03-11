package jfoley.vocabpress.movement;

import jfoley.vocabpress.feature.FeatureMover;
import jfoley.vocabpress.scoring.CountPosting;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static jfoley.vocabpress.scoring.blockiter.movement.AnyOfMoverTest.forDocuments;
import static org.junit.Assert.assertEquals;

public class AllOfMoverTest {

	@Test
	public void testOf() throws Exception {
		FeatureMover<CountPosting> ones = forDocuments((x) -> x*2, 1,2,3,4,5,6,7,8,9,10);
		FeatureMover<CountPosting> twos = forDocuments((x) -> x*4, 2,4,6,8,10);

		Mover mover = AllOfMover.of(ones, twos);
		List<Integer> hits = new ArrayList<>();
		for(; !mover.isDone(); mover.nextBlock()) {
			for(; !mover.isDoneWithBlock(); mover.next()) {
				int doc = mover.currentKey();
				hits.add(doc);
			}
		}

		assertEquals(Arrays.asList(2, 4, 6, 8, 10), hits);
	}
}