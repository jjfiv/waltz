package edu.umass.cs.ciir.waltz.coders.kinds;

import ciir.jfoley.chai.random.Sample;
import edu.umass.cs.ciir.waltz.coders.Coder;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author jfoley
 */
public class LZFCoderTest {
  @Test
  public void testRandomBytes() {
    Random rand = new Random();
    Coder<String> strCoder = new LZFCoder<>(CharsetCoders.utf8);

    for (String str : Sample.strings(rand, 4096)) {
      assertEquals(str, strCoder.read(strCoder.write(str)));
    }
  }

}