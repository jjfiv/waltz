package edu.umass.cs.ciir.waltz.coders.kinds;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author jfoley
 */
public class VarUIntTest {

  @Test
  public void testNegative() {
    VarUInt coder = new VarUInt();
    try {
      assertEquals(-8, coder.read(coder.write(-8)).intValue());
      fail("Shouldn't even try to encode a negative number.");
    } catch (AssertionError e) {
      assertNotNull(e);
    }
  }

  @Test
  public void testImpl() throws Exception {
    VarUInt coder = new VarUInt();
    Random rand = new Random();
    for (int i = 0; i < 1000; i++) {
      int x = Math.abs(rand.nextInt());
      assertEquals(x, coder.read(coder.write(x)).intValue());
    }

    // Check edge-case values:
    assertEquals(0, coder.read(coder.write(0)).intValue());
    assertEquals(1, coder.read(coder.write(1)).intValue());
    assertEquals(Integer.MAX_VALUE, coder.read(coder.write(Integer.MAX_VALUE)).intValue());
  }
}