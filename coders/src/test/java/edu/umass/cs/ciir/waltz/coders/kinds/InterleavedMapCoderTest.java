package edu.umass.cs.ciir.waltz.coders.kinds;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.kinds.InterleavedMapCoder;
import edu.umass.cs.ciir.waltz.coders.kinds.VarInt;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class InterleavedMapCoderTest {

  @Test
  public void testWriteImpl() throws Exception {
    Map<Integer, Integer> data = new HashMap<>();
    Random r = new Random();

    for (int i = 0; i < 1000; i++) {
      data.put(Math.abs(r.nextInt()), r.nextInt());
    }
    Coder<Map<Integer, Integer>> c = new InterleavedMapCoder<>(VarInt.instance, FixedSize.ints);

    assertEquals(data, c.read(c.write(data)));
  }
}