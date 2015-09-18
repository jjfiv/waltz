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
		for(; !mover.isDone(); mover.next()) {
			assertFalse(mover.isDoneWithBlock());
			hits.add(mover.currentKey());
		}
		assertEquals(twoHits, hits);
	}

	@Test
	public void testFailure() {
		Mover elPostings = new IdSetMover(Arrays.asList(9732, 9740, 10080, 22066, 22774, 29635, 32111, 32743, 37910, 38621, 39006, 39426, 44473, 46993, 46994, 51251, 52293, 52294, 52970, 55998, 67135, 67626, 67829, 73361, 76869, 77942, 78492, 78808, 79345, 80982, 88189, 89541, 90215, 91297, 94490, 98692, 104111, 106565, 106940, 106941, 107139, 107790, 109072, 109073, 109314, 114147, 119239, 120861, 124391, 125766, 126262, 126426, 126830, 129570, 131540, 134701, 136717, 137938, 138353, 144377, 144378, 144539, 144543, 144544, 144635, 144711, 144722, 144794, 144824, 144881, 144935, 144977, 144996, 145189, 145277, 145321, 145369, 145421, 145846, 145972, 146302, 148800, 151609, 151610, 151720, 151721, 151722, 151751, 153093, 156626, 157684, 157712, 157894, 157898, 157917, 157940, 158010, 158056, 158643, 159999, 161234, 161235, 161645, 161647, 162166, 162386, 165657, 165764, 165964, 166051, 166271, 166417, 168571, 168675, 168724, 169266, 169277, 169989, 171948, 173135, 173161, 173194, 173235, 173296, 173354, 173485, 173521, 173523));
		Mover ninoPostings = new IdSetMover(Arrays.asList(54331, 88210, 182273, 247752, 263248, 286295, 319927));

		AllOfMover<?> mover = new AllOfMover<>(Arrays.asList(elPostings, ninoPostings));
		System.err.println(mover.currentKey());
		//mover.start();


	}
}