package edu.umass.cs.ciir.waltz.io;

import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.chained.ChaiMap;
import ciir.jfoley.chai.io.TemporaryFile;
import edu.umass.cs.ciir.waltz.io.coders.VByteCoders;
import edu.umass.cs.ciir.waltz.io.galago.GalagoDiskMap;
import edu.umass.cs.ciir.waltz.io.galago.RawGalagoDiskMap;
import edu.umass.cs.ciir.waltz.io.map.IOMap;
import org.junit.Test;
import org.lemurproject.galago.utility.Parameters;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class GalagoDiskMapTest {

  @Test
  public void testGet() throws Exception {
    Parameters testP = Parameters.create();
    testP.put("hello", "world");
    try (TemporaryFile tmpFile = new TemporaryFile("gdmt", "btree")) {
      try (GalagoDiskMap.Writer<Integer, Integer> writer = new GalagoDiskMap.Writer<>(VByteCoders.ints, VByteCoders.ints, tmpFile.getPath(), testP)) {
        writer.put(1,10);
        writer.put(3,30);
      } // close Writer

      try (IOMap<Integer, Integer> reader = RawGalagoDiskMap.createIOMap(VByteCoders.ints, VByteCoders.ints, tmpFile.getPath())) {
        assertEquals("world", reader.getConfig().get("hello"));
        assertEquals(10, reader.get(1).intValue());
        assertEquals(30, reader.get(3).intValue());
        assertEquals(2, reader.keyCount());

        assertEquals(
            ChaiMap.create(Pair.of(1,10), Pair.of(3, 30)),
            reader.getInBulk(Arrays.asList(1,3)));
        assertEquals(
            ChaiMap.create(Pair.of(3, 30)),
            reader.getInBulk(Arrays.asList(3,700)));
      }
    } // delete tmpFile

  }
}