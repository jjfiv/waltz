package edu.umass.cs.ciir.waltz.coders;

import ciir.jfoley.chai.io.TemporaryDirectory;
import ciir.jfoley.chai.random.Sample;
import edu.umass.cs.ciir.waltz.coders.kinds.CharsetCoders;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author jfoley.
 */
public class GenKeyDiskMapTest {

  @Test
  public void buildAndQuery() throws IOException {
    try (TemporaryDirectory tmpdir = new TemporaryDirectory()) {
      String basePath = tmpdir.childPath("test");

      List<String> tmpdata = Sample.strings(new Random(), 1000);

      Coder<String> coder = CharsetCoders.utf8LengthPrefixed;
      try (GenKeyDiskMap.Writer<String> wr = GenKeyDiskMap.Writer.createNew(basePath, coder)) {
        for (int i = 0; i < tmpdata.size(); i++) {
          String str = tmpdata.get(i);
          assertEquals((long) i, wr.writeNextValue(str));
        }
      }

      try (GenKeyDiskMap.Reader rdr = GenKeyDiskMap.Reader.openFiles(basePath)) {
        for (int i = 0; i < tmpdata.size(); i++) {
          assertEquals(tmpdata.get(i), rdr.getValue(coder, i));
        }
      }
    }
  }

}