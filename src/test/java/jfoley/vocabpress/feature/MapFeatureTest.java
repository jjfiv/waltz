package jfoley.vocabpress.feature;

import jfoley.vocabpress.dociter.movement.Mover;
import jfoley.vocabpress.postings.CountPosting;
import jfoley.vocabpress.postings.impl.SimpleCountPosting;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MapFeatureTest {

	@Test
	public void testHasFeature() throws Exception {
		Map<Integer, CountPosting> priors = new HashMap<>();
		priors.put(1, new SimpleCountPosting(1, 9));
		priors.put(2, new SimpleCountPosting(2, 6));
		priors.put(3, new SimpleCountPosting(3, 4));

		MapFeature<CountPosting> fpriors = new MapFeature<>(priors);

		assertTrue(fpriors.hasFeature(1));
		assertTrue(fpriors.hasFeature(2));
		assertTrue(fpriors.hasFeature(3));
		assertEquals(9, fpriors.getFeature(1).getCount());
		assertEquals(6, fpriors.getFeature(2).getCount());
		assertEquals(4, fpriors.getFeature(3).getCount());
	}

	@Test
	public void testMovement() throws Exception {
		Map<Integer, CountPosting> priors = new HashMap<>();
		priors.put(1, new SimpleCountPosting(1, 9));
		priors.put(2, new SimpleCountPosting(2, 6));
		priors.put(3, new SimpleCountPosting(3, 4));

		MapFeature<CountPosting> fpriors = new MapFeature<>(priors);

		int count = 0;
		for(Mover m = fpriors.getMover(); !m.isDone(); m.next()) {
			int doc = m.currentKey();
			count++;
			assertEquals(priors.get(doc).getCount(), fpriors.getFeature(doc).getCount());
			assertEquals(doc, fpriors.getFeature(doc).getKey());
		}

		assertEquals(3, count);
	}


}