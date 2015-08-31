package edu.umass.cs.ciir.waltz.sys;

import ciir.jfoley.chai.collections.IntRange;
import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.chained.ChaiMap;
import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.kinds.CharsetCoders;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapReader;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.sys.counts.CountMetadata;
import edu.umass.cs.ciir.waltz.sys.counts.CountMetadataCoder;
import edu.umass.cs.ciir.waltz.sys.tmp.TmpPostingMerger;
import edu.umass.cs.ciir.waltz.sys.tmp.TmpStreamPostingIndexWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;

/**
 * @author jfoley
 */
public class PostingIndexTest {

  @Test
  public void testStringIndexWriter() throws IOException {
    PostingsConfig<String,CountMetadata, Integer> countsConfig = new PostingsConfig<>(
        CharsetCoders.utf8,
        new CountMetadataCoder(),
        VarUInt.instance,
        Comparator.<String>naturalOrder(),
        new CountMetadata()
    );

    TestTextCountsWriter wr = new TestTextCountsWriter();

    try (TemporaryDirectory tmpdir = new TemporaryDirectory()) {
      try (TmpStreamPostingIndexWriter<String,CountMetadata,Integer> writer = new TmpStreamPostingIndexWriter<>(tmpdir, "counts", countsConfig)) {
        int current = writer.addDocument();
        writer.add("the", current, 1);
        writer.add("quick", current, 3);
        writer.add("fast", current, 3);

        current = writer.addDocument();
        writer.add("the", current, 4);
        writer.add("second", current, 5);

        // flush segment:
        writer.flush();

        current = writer.addDocument();
        writer.add("the", current, 1);
        writer.add("quick", current, 3);
        writer.add("fast", current, 3);

        writer.flush();

        current = writer.addDocument();
        writer.add("the", current, 2);
        writer.add("last", current, 1);

        writer.flush();

        TmpPostingMerger<String, CountMetadata, Integer> merger = writer.getMerger(IntRange.exclusive(0, writer.temporaryIndex));

        merger.write(wr);
      }
    }
    wr.close();

    String expected = "fast\n" +
        "  meta: max:3 total:6 docs:2\n" +
        "    0:3\n" +
        "    2:3\n" +
        "last\n" +
        "  meta: max:1 total:1 docs:1\n" +
        "    3:1\n" +
        "quick\n" +
        "  meta: max:3 total:6 docs:2\n" +
        "    0:3\n" +
        "    2:3\n" +
        "second\n" +
        "  meta: max:5 total:5 docs:1\n" +
        "    1:5\n" +
        "the\n" +
        "  meta: max:4 total:8 docs:4\n" +
        "    0:1\n" +
        "    1:4\n" +
        "    2:1\n" +
        "    3:2\n\n";

    assertEquals(expected, wr.sw.toString());
  }

  private static class TestTextCountsWriter implements PostingIndexWriter<String, CountMetadata, Integer> {
    public final StringWriter sw = new StringWriter();
    public final PrintWriter out = new PrintWriter(sw);

    @Override
    public void writeNewKey(String key) {
      out.println(key);
    }

    @Override
    public void writeMetadata(CountMetadata metadata) {
      out.println("  meta: max:"+metadata.maxCount+" total:"+metadata.totalCount+" docs:"+metadata.totalDocs);
    }

    @Override
    public void writePosting(int doc, Integer value) {
      out.println("    "+doc+":"+value);
    }

    @Override
    public void close() throws IOException {
      out.println();
    }
  }

  @Test
  public void testDiskIndexWriter() throws IOException {
    PostingsConfig<String,CountMetadata, Integer> countsConfig = new PostingsConfig<String,CountMetadata,Integer>(
        CharsetCoders.utf8,
        new CountMetadataCoder(),
        VarUInt.instance,
        Comparator.<String>naturalOrder(),
        new CountMetadata()
    );

    try (TemporaryDirectory tmpdir = new TemporaryDirectory()) {
      try (BlockedPostingsWriter<String, CountMetadata, Integer> finalWriter = new BlockedPostingsWriter<>(countsConfig, tmpdir, "counts")) {
        try (TmpStreamPostingIndexWriter<String, CountMetadata, Integer> writer = new TmpStreamPostingIndexWriter<>(tmpdir, "counts", countsConfig)) {
          int current = writer.addDocument();
          writer.add("the", current, 1);
          writer.add("quick", current, 3);
          writer.add("fast", current, 3);

          current = writer.addDocument();
          writer.add("the", current, 4);
          writer.add("second", current, 5);

          // flush segment:
          writer.flush();

          current = writer.addDocument();
          writer.add("the", current, 1);
          writer.add("quick", current, 3);
          writer.add("fast", current, 3);

          writer.flush();

          current = writer.addDocument();
          writer.add("the", current, 2);
          writer.add("last", current, 1);

          writer.flush();

          TmpPostingMerger<String, CountMetadata, Integer> merger = writer.getMerger(IntRange.exclusive(0, writer.temporaryIndex));

          merger.write(finalWriter);
        }
      }

      try (WaltzDiskMapReader<String, PostingMover<Integer>> countsIndex = countsConfig.openReader(tmpdir, "counts")) {

        assertEquals(countsIndex.get("fast").toMap(), ChaiMap.create(Pair.of(0, 3), Pair.of(2, 3)));
        assertEquals(countsIndex.get("last").toMap(), ChaiMap.create(Pair.of(3, 1)));
        assertEquals(countsIndex.get("quick").toMap(), ChaiMap.create(Pair.of(0, 3), Pair.of(2, 3)));
        assertEquals(countsIndex.get("second").toMap(), ChaiMap.create(Pair.of(1, 5)));
        assertEquals(countsIndex.get("the").toMap(),
            ChaiMap.create(Pair.of(0, 1), Pair.of(1,4), Pair.of(2,1), Pair.of(3,2)));

      }
    }
  }

}