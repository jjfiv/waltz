package edu.umass.cs.ciir.waltz.dbindex;

import edu.umass.cs.ciir.waltz.feature.Feature;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author jfoley.
 */
public class DBIndexTest {

	@Test
	public void testGetAllDocumentIds() throws Exception {
		DBIndex index = new DBIndex(new DBConfig("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"));
		index.addDocument("quack", Arrays.asList("duck", "duck", "goose"));
		index.addDocument("sounds", Arrays.asList("quack", "woof", "meow"));;
		index.addDocument("dog-info", Arrays.asList("dogs", "say", "woof"));

		assertEquals(9, index.getCollectionLength());
		assertEquals(3, index.getDocumentCount());

		int soundsId = index.getDocumentId("sounds");
		assertEquals("sounds", index.getDocumentName(soundsId));

		Feature<Integer> lengths = index.getLengths();
		assertNotNull(lengths);
		assertTrue(lengths.hasFeature(soundsId));
		assertEquals(3, lengths.getFeature(soundsId).intValue());
	}

}