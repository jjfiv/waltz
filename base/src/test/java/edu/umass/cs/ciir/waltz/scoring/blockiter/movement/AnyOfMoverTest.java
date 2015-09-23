package edu.umass.cs.ciir.waltz.scoring.blockiter.movement;

import ciir.jfoley.chai.fn.TransformFn;
import edu.umass.cs.ciir.waltz.dociter.BlockPostingsIterator;
import edu.umass.cs.ciir.waltz.dociter.ListBlockPostingsIterator;
import edu.umass.cs.ciir.waltz.dociter.movement.AnyOfMover;
import edu.umass.cs.ciir.waltz.dociter.movement.BlockPostingsMover;
import edu.umass.cs.ciir.waltz.dociter.movement.IdSetMover;
import edu.umass.cs.ciir.waltz.postings.Posting;
import edu.umass.cs.ciir.waltz.postings.SimplePosting;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

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
      //System.out.println(doc);

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

	@Test
	public void realWorld1() {
		List<Integer> elData = Arrays.asList(742, 787, 2016, 2548, 2549, 2647, 2972, 3063, 3341, 3862, 4204, 4729, 5031, 5591, 5792, 6418, 6772, 6814, 6891, 7993, 8149, 8264, 8541, 8565, 8755, 8850, 8854, 9262, 9300, 9555, 9821, 10098, 10772, 11255, 11526, 11914, 12106, 12273, 12277, 12503, 13256, 13526, 13731, 14096, 15004, 15618, 16135, 16188, 16353, 16361, 16649, 16766, 17374, 17377, 17816, 17817, 17819, 17820, 17821, 17824, 17829, 17830, 17934, 18848, 19369, 19561, 19785, 19820, 19993, 20063, 20265, 20278, 20431, 20554, 20627, 21265, 21311, 21336, 21629, 21770, 21816, 22075, 22328, 22422, 22467, 23612, 23682, 23701, 24457, 24623, 24860, 25447, 25472, 25479, 25584, 25984, 26460, 26656, 27038, 27413, 27706, 27772, 28183, 28187, 28629, 28631, 28633, 28634, 28778, 29014, 29016, 29517, 29525, 29962, 30044, 30220, 30864, 31195, 31996, 32199, 32397, 32398, 32404, 32408, 32670, 33721, 33726, 33903);
		IdSetMover elPostings = new IdSetMover(elData);
		List<Integer> ninoData = Arrays.asList(5344, 5554, 6789, 6797, 11255, 18452, 27292, 30220, 51101, 71024, 71188, 71384, 74468, 76726, 83752, 88608, 90058, 92498, 95516, 96086, 96373, 97557, 98359, 101751, 108595, 113535, 116949, 132511, 134443, 144401, 144802, 150708, 152312, 154542, 155091, 155355, 155691, 164788, 174535, 177075, 191652, 204673, 206275, 214025, 214333, 215963, 216945, 218646, 227954, 229217, 229291, 237062, 237522, 239688, 239782, 242976, 244770, 263810, 292684, 300287, 301009, 301172, 303646, 311315, 329201, 335831, 336408, 336783, 337401, 338374, 340687, 340927, 343724, 344491, 348361, 350329, 351492, 351620, 353730, 354567, 357743, 358201, 358587, 359137, 364653, 366387, 366521, 368204, 370529, 372174, 374681, 378171, 379042, 379660, 382607, 383553, 385600, 387399, 387527, 387833, 388072, 389004, 390214, 393864, 398007, 400218, 401942, 402109, 402801, 418692, 425302, 426128, 429757, 437480, 437519, 438791, 439856, 443068, 445370, 448619, 450801, 464130, 491053, 491755, 506636);
		IdSetMover ninoPostings = new IdSetMover(ninoData);

		AnyOfMover<?> mover = new AnyOfMover<>(Arrays.asList(elPostings, ninoPostings));
		ArrayList<Integer> ids = new ArrayList<>();
		mover.execute((docId) -> {
			assertTrue(elPostings.matches(docId) || ninoPostings.matches(docId));
			ids.add(docId);
		});
		TreeSet<Integer> inMemoryMerge = new TreeSet<>();
		inMemoryMerge.addAll(elData);
		inMemoryMerge.addAll(ninoData);
		assertEquals(new ArrayList<>(inMemoryMerge), ids);
	}
}