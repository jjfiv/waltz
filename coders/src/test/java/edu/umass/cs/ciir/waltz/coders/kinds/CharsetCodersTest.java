package edu.umass.cs.ciir.waltz.coders.kinds;

import ciir.jfoley.chai.random.Sample;
import edu.umass.cs.ciir.waltz.coders.Coder;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author jfoley
 */
public class CharsetCodersTest {
  @Test
  public void testStrings() throws IOException {
    Coder<String> utf8 = CharsetCoders.utf8;
    for (String str : Sample.strings(new Random(), 1000)) {
      assertEquals(str, utf8.read(utf8.writeImpl(str)));
    }
  }

}