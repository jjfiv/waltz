package edu.umass.cs.ciir.waltz.sys;

import ciir.jfoley.chai.collections.IntRange;
import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.chained.ChaiMap;
import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.CharsetCoders;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapReader;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.io.postings.format.BlockedPostingsCoder;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;

/**
 * @author jfoley
 */
public class PostingIndexTest {
  public static class CountMetadata implements PostingIndex.KeyMetadata<Integer, CountMetadata> {
    public int totalDocs = 0;
    public int maxCount = 0;
    public int totalCount = 0;

    @Override
    public int totalDocuments() {
      return totalDocs;
    }

    @Override
    public void accumulate(CountMetadata o) {
      this.totalDocs += o.totalDocs;
      this.totalCount += o.totalCount;
      this.maxCount = Math.max(this.maxCount, o.maxCount);
    }

    @Override
    public void accumulate(int document, Integer item) {
      totalDocs++;
      totalCount += item;
      maxCount = Math.max(maxCount, item);
    }

    @Override
    public CountMetadata zero() {
      return new CountMetadata();
    }
  }

  public static class CountMetadataCoder extends Coder<CountMetadata> {
    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Nonnull
    @Override
    public DataChunk writeImpl(CountMetadata m) throws IOException {
      ByteBuilder bb = new ByteBuilder();
      bb.add(FixedSize.ints, m.totalDocs);
      bb.add(FixedSize.ints, m.maxCount);
      bb.add(FixedSize.ints, m.totalCount);
      return bb;
    }

    @Nonnull
    @Override
    public CountMetadata readImpl(InputStream inputStream) throws IOException {
      CountMetadata m = new CountMetadata();
      m.totalDocs = FixedSize.ints.readImpl(inputStream);
      m.maxCount = FixedSize.ints.readImpl(inputStream);
      m.totalCount = FixedSize.ints.readImpl(inputStream);
      return m;
    }
  }

  @Test
  public void testStringIndexWriter() throws IOException {
    PostingIndex.PostingsConfig<String,CountMetadata, Integer> countsConfig = new PostingIndex.PostingsConfig<String,CountMetadata,Integer>(
        CharsetCoders.utf8,
        new CountMetadataCoder(),
        VarUInt.instance,
        Comparator.<String>naturalOrder(),
        new CountMetadata()
    );

    TestTextCountsWriter wr = new TestTextCountsWriter();

    try (TemporaryDirectory tmpdir = new TemporaryDirectory()) {
      try (PostingIndex.TmpStreamPostingIndexWriter<String,CountMetadata,Integer> writer = new PostingIndex.TmpStreamPostingIndexWriter<>(tmpdir, "counts", countsConfig)) {
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

        PostingIndex.TmpPostingMerger<String, CountMetadata, Integer> merger = writer.getMerger(IntRange.exclusive(0, writer.temporaryIndex));

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

  private static class TestTextCountsWriter implements PostingIndex.PostingIndexWriter<String, CountMetadata, Integer> {
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
    PostingIndex.PostingsConfig<String,CountMetadata, Integer> countsConfig = new PostingIndex.PostingsConfig<String,CountMetadata,Integer>(
        CharsetCoders.utf8,
        new CountMetadataCoder(),
        VarUInt.instance,
        Comparator.<String>naturalOrder(),
        new CountMetadata()
    );

    try (TemporaryDirectory tmpdir = new TemporaryDirectory()) {
      try (PostingIndex.BlockedPostingsWriter<String, CountMetadata, Integer> finalWriter = new PostingIndex.BlockedPostingsWriter<>(countsConfig, tmpdir, "counts")) {
        try (PostingIndex.TmpStreamPostingIndexWriter<String, CountMetadata, Integer> writer = new PostingIndex.TmpStreamPostingIndexWriter<>(tmpdir, "counts", countsConfig)) {
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

          PostingIndex.TmpPostingMerger<String, CountMetadata, Integer> merger = writer.getMerger(IntRange.exclusive(0, writer.temporaryIndex));

          merger.write(finalWriter);
        }
      }

      try (WaltzDiskMapReader<String, PostingMover<Integer>> countsIndex = new WaltzDiskMapReader<>(
          tmpdir, "counts",
          CharsetCoders.utf8, new BlockedPostingsCoder<>(VarUInt.instance)
      )) {

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