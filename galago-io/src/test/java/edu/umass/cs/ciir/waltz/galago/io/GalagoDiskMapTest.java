package edu.umass.cs.ciir.waltz.galago.io;

import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.io.TemporaryFile;
import ciir.jfoley.chai.random.Sample;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.CharsetCoders;
import edu.umass.cs.ciir.waltz.coders.kinds.VarInt;
import edu.umass.cs.ciir.waltz.coders.map.IOMap;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriter;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriterRawWrapper;
import edu.umass.cs.ciir.waltz.galago.io.coders.GalagoVByteCoders;
import org.junit.Test;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.*;

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
            Collections.singletonList(Pair.of(3, 30)),
            reader.getInBulk(Arrays.asList(3,700)));
      }
    } // delete tmpFile

  }

  @Test
  public void testRandomStrings() throws IOException {
    List<String> keys = Sample.strings(new Random(), 1000);
    List<String> values = Sample.strings(new Random(), 1000);
    Map<String,String> actualData = new HashMap<>();
    for (int i = 0; i < keys.size(); i++) {
      actualData.put(keys.get(i), values.get(i));
    }

    Coder<String> strCoder = CharsetCoders.utf8;

    try (TemporaryFile tmpFile = new TemporaryFile("test", ".btree")) {
      // write:
      try (IOMapWriter<String, String> ioMapWriter = GalagoIO.getIOMapWriter(strCoder, strCoder, tmpFile.getPath())) {
        for (Map.Entry<String, String> kv : actualData.entrySet()) {
          ioMapWriter.put(kv.getKey(), kv.getValue());
        }
      }

      // read:
      try (IOMap<String, String> ioMap = GalagoIO.openIOMap(strCoder, strCoder, tmpFile.getPath())) {
        assertEquals(actualData.size(), ioMap.keyCount());


        for (Map.Entry<String, String> kv : actualData.entrySet()) {
          String fetchedValue = ioMap.get(kv.getKey());
          assertNotNull(fetchedValue);
          assertEquals(kv.getValue(), fetchedValue);
        }
      }

    } // delete tmpFile
  }

  @Test
  public void testRandomInts() throws IOException {
    Map<Integer,Integer> actualData = new HashMap<>();
    List<Integer> keys = Sample.randomIntegers(1000, 50000);
    List<Integer> values = Sample.randomIntegers(1000, 50000);
    for (int i = 0; i < keys.size(); i++) {
      actualData.put(keys.get(i), values.get(i));
    }

    Coder<Integer> itemCoder = VarInt.instance;

    try (TemporaryFile tmpFile = new TemporaryFile("test", ".btree")) {
      // write:
      try (IOMapWriter<Integer, Integer> ioMapWriter = GalagoIO.getIOMapWriter(itemCoder, itemCoder, tmpFile.getPath())) {
        for (Map.Entry<Integer, Integer> kv : actualData.entrySet()) {
          ioMapWriter.put(kv.getKey(), kv.getValue());
        }
      }

      // read:
      try (IOMap<Integer, Integer> ioMap = GalagoIO.openIOMap(itemCoder, itemCoder, tmpFile.getPath())) {
        assertEquals(actualData.size(), ioMap.keyCount());
        for (Map.Entry<Integer, Integer> kv : actualData.entrySet()) {
          int fetchedValue = ioMap.get(kv.getKey());
          assertEquals(kv.getValue().intValue(), fetchedValue);
        }
      }

    } // delete tmpFile
  }

}