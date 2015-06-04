package edu.umass.cs.ciir.waltz.io.postings;

import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.chained.ChaiIterable;
import ciir.jfoley.chai.collections.util.ListFns;
import ciir.jfoley.chai.random.Sample;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.postings.extents.ExtentsList;
import edu.umass.cs.ciir.waltz.postings.extents.InterleavedExtents;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author jfoley
 */
public class ExtentListCoderTest {

  @Test
  public void testExtentListCoder() {
    Coder<ExtentsList> coder = new ExtentListCoder();
    InterleavedExtents extents = new InterleavedExtents();
    List<Integer> begins = ChaiIterable
        .create(Sample.randomIntegers(1000, 5000))
        .map(Math::abs)
        .sorted()
        .intoList();
    List<Integer> deltas = ChaiIterable
        .create(Sample.randomIntegers(1000, 5000))
        .map(Math::abs)
        .map((x) -> Math.max(1, x))
        .intoList();

    for (Pair<Integer, Integer> kv : ListFns.zip(begins, deltas)) {
      int begin = Math.abs(kv.getKey());
      int delta = Math.max(1, Math.abs(kv.getValue()));
      extents.push(begin, begin+delta);
    }
    assertEquals(1000, extents.size());

    ByteBuffer data = coder.write(extents);
    ExtentsList decoded = coder.read(data);
    assertEquals(extents, decoded);
  }

}