package edu.umass.cs.ciir.waltz.io.coders;

import edu.umass.cs.ciir.waltz.io.Coder;
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
    Coder<Map<Integer, Integer>> c = new InterleavedMapCoder<>(VByteCoders.ints, FixedSize.ints);

    assertEquals(data, c.read(c.write(data)));
  }
}