package edu.umass.cs.ciir.waltz.coders;

import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapReader;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapWriter;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author jfoley
 */
public class WaltzDiskMapTest {

  @Test
  public void testWaltzDiskMap() throws IOException {
    try(TemporaryDirectory tmpdir = new TemporaryDirectory()) {
      try (WaltzDiskMapWriter<Integer, Integer> writer = new WaltzDiskMapWriter<>(tmpdir, "strs", FixedSize.ints, FixedSize.ints)) {
        for (int i = 0; i < 1000; i++) {
          writer.put(i, i*100);
        }
      }

      try (WaltzDiskMapReader<Integer, Integer> reader = new WaltzDiskMapReader<>(tmpdir, "strs", FixedSize.ints, FixedSize.ints)) {
        assertNull(reader.get(1001));
        assertNull(reader.get(-1001));
        for (int i = 0; i < 1000; i++) {
          Integer x = reader.get(i);
          assertNotNull(x);
          assertEquals(i * 100, x.intValue());
        }
      }
    }
  }

}