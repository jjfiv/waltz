package edu.umass.cs.ciir.waltz.dociter.movement;

import ciir.jfoley.chai.collections.IntRange;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AllOfMoverTest {

	@Test
	public void testSimple() throws Exception {
		Mover ones = IdSetMover.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		Mover twos = IdSetMover.of(2,4,6,8,10);

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

	@Test
	public void testThree() throws Exception {
		Mover ones = IdSetMover.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		Mover twos = IdSetMover.of(2,4,6,8,10);
		Mover threes = IdSetMover.of(3,6,9);

		Mover mover = AllOfMover.of(ones, twos, threes);
		List<Integer> hits = new ArrayList<>();
		for(; !mover.isDone(); mover.nextBlock()) {
			for(; !mover.isDoneWithBlock(); mover.next()) {
				int doc = mover.currentKey();
				hits.add(doc);
			}
		}

		assertEquals(Arrays.asList(6), hits);
	}

	@Test
	public void testXL() throws Exception {
		Mover xl = new IdSetMover(IntRange.exclusive(0, 20000));
		List<Integer> twoHits = Arrays.asList(2, 4, 6, 8, 10);
		Mover twos = new IdSetMover(twoHits);

		Mover mover = AllOfMover.of(xl, twos);
		List<Integer> hits = new ArrayList<>();
		for(; !mover.isDone(); mover.nextBlock()) {
			for(; !mover.isDoneWithBlock(); mover.next()) {
				int doc = mover.currentKey();
				hits.add(doc);
			}
		}

		assertEquals(twoHits, hits);
	}

	@Test
	public void testCommonRare() {
		Mover xl = new IdSetMover(IntRange.exclusive(0, 20000));
		List<Integer> twoHits = Arrays.asList(302,304,306,308,310);
		Mover twos = new IdSetMover(twoHits);

		Mover mover = AllOfMover.of(xl, twos);
		List<Integer> hits = new ArrayList<>();
		for(; !mover.isDone(); mover.nextBlock()) {
			for(; !mover.isDoneWithBlock(); mover.next()) {
				int doc = mover.currentKey();
				hits.add(doc);
			}
		}

		assertEquals(twoHits, hits);
	}

	@Test
	public void testCommonRare2() {
		Mover xl = new IdSetMover(IntRange.exclusive(0, 20000));
		List<Integer> twoHits = Arrays.asList(302,304,306,308,310);
		Mover twos = new IdSetMover(twoHits);

		AllOfMover<AMover> mover = AllOfMover.<AMover>of((AMover) xl, (AMover) twos);
		List<Integer> hits = new ArrayList<>();
		for(mover.start(); !mover.isDone(); mover.next()) {
			assertFalse(mover.isDoneWithBlock());
			hits.add(mover.currentKey());
		}
		assertEquals(twoHits, hits);
	}
}