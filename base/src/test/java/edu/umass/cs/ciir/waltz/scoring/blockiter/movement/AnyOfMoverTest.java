package edu.umass.cs.ciir.waltz.scoring.blockiter.movement;

import ciir.jfoley.chai.fn.TransformFn;
import edu.umass.cs.ciir.waltz.dociter.BlockPostingsIterator;
import edu.umass.cs.ciir.waltz.dociter.ListBlockPostingsIterator;
import edu.umass.cs.ciir.waltz.postings.SimplePosting;
import edu.umass.cs.ciir.waltz.dociter.movement.AnyOfMover;
import edu.umass.cs.ciir.waltz.dociter.movement.BlockPostingsMover;
import edu.umass.cs.ciir.waltz.postings.Posting;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AnyOfMoverTest {

	@Test
	public void testSimple() throws Exception {
		BlockPostingsIterator<Integer> lhsData = new ListBlockPostingsIterator<>(Arrays.asList(
			SimplePosting.Count(1, 1),
			SimplePosting.Count(3, 3),
			SimplePosting.Count(5, 5),
			SimplePosting.Count(7, 7),
			SimplePosting.Count(9, 9)
		), 2);
		BlockPostingsIterator<Integer> rhsData = new ListBlockPostingsIterator<>(Arrays.asList(
			SimplePosting.Count(0, 0),
			SimplePosting.Count(2, 4),
			SimplePosting.Count(5, 10),
			SimplePosting.Count(8, 16)
		), 3);

		BlockPostingsMover<Integer> lhs = new BlockPostingsMover<>(lhsData);
		BlockPostingsMover<Integer> rhs = new BlockPostingsMover<>(rhsData);
		AnyOfMover mover = new AnyOfMover(Arrays.asList(lhs, rhs));

		List<Integer> hits = new ArrayList<>();
		for(; !mover.isDone(); mover.next()) {
			int doc = mover.currentKey();
			hits.add(doc);

			if(lhs.matches(doc)) {
				Assert.assertEquals(doc, lhs.getCurrentPosting().intValue());
			}
			if(rhs.matches(doc)) {
        Assert.assertEquals(doc * 2, rhs.getCurrentPosting().intValue());
			}
			if(lhs.matches(doc) && rhs.matches(doc)) {
				assertEquals(5, doc);
			}
		}

		assertEquals(Arrays.asList(0, 1, 2, 3, 5, 7, 8, 9), hits);
		assertTrue(lhs.isDoneWithBlock());
		assertTrue(rhs.isDoneWithBlock());
		assertTrue(rhs.isDone());
		assertTrue(lhs.isDone());
	}

	public static BlockPostingsMover<Integer> forDocuments(TransformFn<Integer,Integer> mapper, int... docids) {
		List<Posting<Integer>> output = new ArrayList<>();
		for (int docid : docids) {
			output.add(SimplePosting.Count(docid, mapper.transform(docid)));
		}
		return new BlockPostingsMover<>(new ListBlockPostingsIterator<>(output, 3));
	}

	@Test
	public void testNested() throws Exception {

		BlockPostingsMover<Integer> twos = forDocuments((x) -> x*2, 1,2,3,4,15);
		BlockPostingsMover<Integer> threes = forDocuments((x) -> x*3, 3,4,5,6,20);
		BlockPostingsMover<Integer> fours = forDocuments((x) -> x*4, 7,8,9,10,30);

		AnyOfMover twothree = AnyOfMover.of(twos, threes);
		AnyOfMover mover = AnyOfMover.of(twothree, twos, threes, fours);

		List<Integer> hits = new ArrayList<>();
		for(; !mover.isDone(); mover.next()) {
			int doc = mover.currentKey();
      System.out.println(doc);

      if(hits.size() > 1000) throw new RuntimeException();
			hits.add(doc);

			if(twos.matches(doc)) {
				Assert.assertEquals(doc * 2, twos.getCurrentPosting().intValue());
			}
			if(threes.matches(doc)) {
				Assert.assertEquals(doc * 3, threes.getCurrentPosting().intValue());
			}
			if(fours.matches(doc)) {
				Assert.assertEquals(doc * 4, fours.getCurrentPosting().intValue());
			}
		}

		assertEquals(Arrays.asList(1,2,3,4,5,6,7,8,9,10,15,20,30), hits);
	}
}