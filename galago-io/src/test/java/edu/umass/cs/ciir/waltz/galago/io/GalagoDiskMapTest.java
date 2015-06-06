package edu.umass.cs.ciir.waltz.galago.io;

import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.io.TemporaryFile;
import ciir.jfoley.chai.random.Sample;
import edu.umass.cs.ciir.waltz.coders.kinds.CharsetCoders;
import edu.umass.cs.ciir.waltz.coders.map.IOMap;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriter;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriterRawWrapper;
import edu.umass.cs.ciir.waltz.galago.io.coders.GalagoVByteCoders;
import org.junit.Test;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GalagoDiskMapTest {

  @Test
  public void testGet() throws Exception {
    Parameters testP = Parameters.create();
    testP.put("hello", "world");
    try (TemporaryFile tmpFile = new TemporaryFile("gdmt", "btree")) {
      try (IOMapWriterRawWrapper<Integer, Integer> writer = GalagoIO.getIOMapWriter(GalagoVByteCoders.ints, GalagoVByteCoders.ints, tmpFile.getPath(), testP)) {
        writer.put(1,10);
        writer.put(3,30);
      } // close Writer

      try (IOMap<Integer, Integer> reader = GalagoIO.openIOMap(GalagoVByteCoders.ints, GalagoVByteCoders.ints, tmpFile.getPath())) {
        assertEquals("world", reader.getConfig().get("hello"));
        assertEquals(10, reader.get(1).intValue());
        assertEquals(30, reader.get(3).intValue());
        assertEquals(2, reader.keyCount());

        assertEquals(
            Arrays.asList(Pair.of(1,10), Pair.of(3, 30)),
            reader.getInBulk(Arrays.asList(1,3)));
        assertEquals(
            Arrays.asList(Pair.of(3, 30)),
            reader.getInBulk(Arrays.asList(3,700)));
      }
    } // delete tmpFile

  }

  @Test
  public void testRandomStrings() throws IOException {
    List<String> keys = Sample.strings(new Random(), 1000);
    List<String> values = Sample.strings(new Random(), 1000);

    //List<String> keys = Arrays.asList("1", "2", "12", "13");
    //List<String> values = Arrays.asList("11", "22", "1212", "1313");

    try (TemporaryFile tmpFile = new TemporaryFile("test", ".btree")) {
      // write:
      try (IOMapWriter<String, String> ioMapWriter = GalagoIO.getIOMapWriter(CharsetCoders.utf8Raw.lengthSafe(), CharsetCoders.utf8Raw.lengthSafe(), tmpFile.getPath())) {
        for (int i = 0; i < keys.size(); i++) {
          ioMapWriter.put(keys.get(i), values.get(i));
        }
      }

      // read:
      try (IOMap<String, String> ioMap = GalagoIO.openIOMap(CharsetCoders.utf8Raw.lengthSafe(), CharsetCoders.utf8Raw.lengthSafe(), tmpFile.getPath())) {
        for (int i = 0; i < keys.size(); i++) {
          String fetchedValue = ioMap.get(keys.get(i));
          assertNotNull(fetchedValue);
          System.out.println(Arrays.toString(values.get(i).getBytes()));
          System.out.println(Arrays.toString(fetchedValue.getBytes()));
          assertEquals(values.get(i), fetchedValue);
        }
      }

    } // delete tmpFile
  }
}