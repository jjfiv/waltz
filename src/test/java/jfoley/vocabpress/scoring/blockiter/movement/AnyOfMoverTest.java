package jfoley.vocabpress.scoring.blockiter.movement;

import ciir.jfoley.chai.fn.TransformFn;
import jfoley.vocabpress.dociter.movement.AnyOfMover;
import jfoley.vocabpress.dociter.movement.BlockPostingsMover;
import jfoley.vocabpress.postings.CountPosting;
import jfoley.vocabpress.dociter.BlockPostingsIterator;
import jfoley.vocabpress.dociter.ListBlockPostingsIterator;
import jfoley.vocabpress.postings.impl.SimpleCountPosting;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AnyOfMoverTest {

	@Test
	public void testSimple() throws Exception {
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

		BlockPostingsMover<CountPosting> lhs = new BlockPostingsMover<>(lhsData);
		BlockPostingsMover<CountPosting> rhs = new BlockPostingsMover<>(rhsData);
		AnyOfMover mover = new AnyOfMover(Arrays.asList(lhs, rhs));

		List<Integer> hits = new ArrayList<>();
		for(; !mover.isDone(); mover.next()) {
			int doc = mover.currentKey();
			hits.add(doc);

			if(lhs.matches(doc)) {
				CountPosting lcount = lhs.getCurrentPosting();
				assertEquals(doc, lcount.getKey());
				assertEquals(doc, lcount.getCount());
			}
			if(rhs.matches(doc)) {
				CountPosting rcount = rhs.getCurrentPosting();
				assertEquals(doc, rcount.getKey());
				assertEquals(doc*2, rcount.getCount());
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

	public static BlockPostingsMover<CountPosting> forDocuments(TransformFn<Integer,Integer> mapper, int... docids) {
		List<CountPosting> output = new ArrayList<>();
		for (int docid : docids) {
			output.add(new SimpleCountPosting(docid, mapper.transform(docid)));
		}
		return new BlockPostingsMover<>(new ListBlockPostingsIterator<>(output, 3));
	}

	@Test
	public void testNested() throws Exception {

		BlockPostingsMover<CountPosting> twos = forDocuments((x) -> x*2, 1,2,3,4,15);
		BlockPostingsMover<CountPosting> threes = forDocuments((x) -> x*3, 3,4,5,6,20);
		BlockPostingsMover<CountPosting> fours = forDocuments((x) -> x*4, 7,8,9,10,30);

		AnyOfMover twothree = AnyOfMover.of(twos, threes);
		AnyOfMover mover = AnyOfMover.of(twothree, twos, threes, fours);

		List<Integer> hits = new ArrayList<>();
		for(; !mover.isDone(); mover.next()) {
			int doc = mover.currentKey();
      System.out.println(doc);

      if(hits.size() > 1000) throw new RuntimeException();
			hits.add(doc);

			if(twos.matches(doc)) {
				CountPosting p = twos.getCurrentPosting();
				assertEquals(doc, p.getKey());
				assertEquals(doc*2, p.getCount());
			}
			if(threes.matches(doc)) {
				CountPosting p = threes.getCurrentPosting();
				assertEquals(doc, p.getKey());
				assertEquals(doc*3, p.getCount());
			}
			if(fours.matches(doc)) {
				CountPosting p = fours.getCurrentPosting();
				assertEquals(doc, p.getKey());
				assertEquals(doc*4, p.getCount());
			}
		}

		assertEquals(Arrays.asList(1,2,3,4,5,6,7,8,9,10,15,20,30), hits);
	}
}