package edu.umass.cs.ciir.waltz.postings.docset;

import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.kinds.CharsetCoders;
import edu.umass.cs.ciir.waltz.coders.map.IOMap;
import edu.umass.cs.ciir.waltz.dociter.movement.Mover;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author jfoley
 */
public class DocumentSetWriterTest {
  @Test
  public void testDocumentSet() throws IOException {
    try (TemporaryDirectory tmpdir = new TemporaryDirectory()) {
      try (DocumentSetWriter<String> labels = new DocumentSetWriter<>(CharsetCoders.utf8, tmpdir, "labels")
      ) {
        labels.process("blarg", 1);
        labels.process("blarg", 3);
        labels.process("asdf", 1);
        labels.process("asdf", 2);
        labels.process("asdf", 3);
      }

      try (IOMap<String, Mover> dsr = new DocumentSetReader<>(CharsetCoders.utf8, tmpdir, "labels")) {
        List<Integer> asdf = new IntList();
        Objects.requireNonNull(dsr.get("asdf")).execute(asdf::add);
        assertEquals(Arrays.asList(1,2,3), asdf);

        List<Integer> blarg = new IntList();
        Objects.requireNonNull(dsr.get("blarg")).execute(blarg::add);
        assertEquals(Arrays.asList(1, 3), blarg);
      }
    }
  }

  @Test
  public void largeNoisyTest() throws IOException {

    IntList data1 = new IntList();
    String key = "key";

    int N = 30000;
    data1.reserve(N);
    for (int i = 0; i < N; i++) {
      data1.add(i*3);
    }
    Collections.shuffle(data1);

    try (TemporaryDirectory tmpdir = new TemporaryDirectory()) {
      try (DocumentSetWriter<String> labels = new DocumentSetWriter<>(CharsetCoders.utf8, tmpdir, "labels")) {
        for (int doc : data1) {
          labels.process("key", doc);
        }
      }

      Collections.sort(data1);
      try (IOMap<String, Mover> dsr = new DocumentSetReader<>(CharsetCoders.utf8, tmpdir, "labels")) {
        Mover data = dsr.get(key);
        assertNotNull(data);
        IntList other = new IntList();
        data.execute(other::add);
        assertEquals(data1, other);
      }

    }
  }

}