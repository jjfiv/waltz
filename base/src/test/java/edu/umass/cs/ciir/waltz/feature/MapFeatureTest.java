package edu.umass.cs.ciir.waltz.feature;

import edu.umass.cs.ciir.waltz.dociter.movement.Mover;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MapFeatureTest {

	@Test
	public void testHasFeature() throws Exception {
		Map<Integer, Integer> priors = new HashMap<>();
		priors.put(1, 9);
		priors.put(2, 6);
		priors.put(3, 4);

		MapFeature<Integer> fpriors = new MapFeature<>(priors);

		assertTrue(fpriors.hasFeature(1));
		assertTrue(fpriors.hasFeature(2));
		assertTrue(fpriors.hasFeature(3));
		Assert.assertEquals(9, fpriors.getFeature(1).intValue());
		Assert.assertEquals(6, fpriors.getFeature(2).intValue());
		Assert.assertEquals(4, fpriors.getFeature(3).intValue());
	}

	@Test
	public void testMovement() throws Exception {
		Map<Integer, Integer> priors = new HashMap<>();
		priors.put(1, 9);
		priors.put(2, 6);
		priors.put(3, 4);

		MapFeature<Integer> fpriors = new MapFeature<>(priors);

		int count = 0;
		for(Mover m = fpriors.getAsMover(); !m.isDone(); m.next()) {
			int doc = m.currentKey();
			count++;
			Assert.assertEquals(priors.get(doc).intValue(), fpriors.getFeature(doc).intValue());
		}

		assertEquals(3, count);
	}


}