package edu.umass.cs.ciir.waltz.io.coders;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.coders.kinds.VarInt;
import edu.umass.cs.ciir.waltz.galago.io.coders.DeltaIntListCoder;
import edu.umass.cs.ciir.waltz.galago.io.coders.ListCoder;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeltaIntListCoderTest {

  @Test
  public void testRandomly() throws Exception {
    IntList testData = new IntList();
    Random r = new Random();

    for (int i = 0; i < 10000; i++) {
      testData.add(Math.abs(r.nextInt()));
    }
    Collections.sort(testData);

    ListCoder<Integer> lc = new ListCoder<>(VarInt.instance);
    DeltaIntListCoder dlc = new DeltaIntListCoder();

    ByteBuffer withoutDeltas = lc.write(testData);
    ByteBuffer withDeltas = dlc.write(testData);


    //System.out.println(withoutDeltas.limit()+" > "+withDeltas.limit());

    // This is the whole reason we're doing it...
    assertTrue(withoutDeltas.limit() >= withDeltas.limit());

    assertEquals(testData, lc.read(withoutDeltas));
    assertEquals(testData, dlc.read(withDeltas));
  }
}