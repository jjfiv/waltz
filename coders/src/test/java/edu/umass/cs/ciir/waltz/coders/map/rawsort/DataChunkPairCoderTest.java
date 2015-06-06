package edu.umass.cs.ciir.waltz.coders.map.rawsort;

import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.random.Sample;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.CharsetCoders;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author jfoley.
 */
public class DataChunkPairCoderTest {

  @Test
  public void testDataChunkPairCoder() {
    List<String> keys = Sample.strings(new Random(), 1000);
    List<String> values = Sample.strings(new Random(), 1000);
    Coder<String> strCoder = CharsetCoders.utf8Raw;
    DataChunkPairCoder coder = new DataChunkPairCoder();

    for (int i = 0; i < keys.size(); i++) {
      DataChunkPair pair = new DataChunkPair(
          strCoder.writeData(keys.get(i)),
          strCoder.writeData(values.get(i)));

      // original pair still works:
      Pair<String, String> strs = pair.decode(strCoder, strCoder);
      assertEquals(keys.get(i), strs.getKey());
      assertEquals(values.get(i), strs.getValue());

      // encode/decode; at least keys sane:
      DataChunkPair transformed = coder.read(coder.write(pair));
      assertEquals(pair.getKey(), transformed.getKey());

      // transformed pair still works:
      Pair<String, String> strs2 = pair.decode(strCoder, strCoder);
      assertEquals(keys.get(i), strs2.getKey());
      assertEquals(values.get(i), strs2.getValue());
    }
  }

}