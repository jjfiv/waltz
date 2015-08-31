package edu.umass.cs.ciir.waltz.sys;

import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.io.Directory;
import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.map.IOMap;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.io.postings.PositionsListCoder;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class PositionsIndexFile {
  public static class PositionsCountMetadata implements KeyMetadata<PositionsList, PositionsCountMetadata> {

    public int totalDocs = 0;
    public int maxCount = 0;
    public int totalCount = 0;
    public int highestPosition = 0;

    @Override
    public int totalDocuments() {
      return totalDocs;
    }

    @Override
    public void accumulate(PositionsCountMetadata m) {
      totalDocs += m.totalDocs;
      totalCount += m.totalCount;
      maxCount = Math.max(maxCount, m.maxCount);
      highestPosition = Math.max(highestPosition, m.highestPosition);
    }

    @Override
    public void accumulate(int document, PositionsList item) {
      totalDocs++;
      int count = item.size();
      totalCount += count;
      maxCount = Math.max(count, maxCount);
      highestPosition = Math.max(highestPosition, item.getPosition(count - 1));
    }

    @Override
    public PositionsCountMetadata zero() {
      return new PositionsCountMetadata();
    }
  }
  public static class PositionsCountMetadataCoder extends Coder<PositionsCountMetadata> {
    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Nonnull
    @Override
    public DataChunk writeImpl(PositionsCountMetadata m) throws IOException {
      ByteBuilder bb = new ByteBuilder();
      bb.add(FixedSize.ints, m.totalDocs);
      bb.add(FixedSize.ints, m.maxCount);
      bb.add(FixedSize.ints, m.totalCount);
      bb.add(FixedSize.ints, m.highestPosition);
      return bb;
    }

    @Nonnull
    @Override
    public PositionsCountMetadata readImpl(InputStream inputStream) throws IOException {
      PositionsCountMetadata m = new PositionsCountMetadata();
      m.totalDocs = FixedSize.ints.readImpl(inputStream);
      m.maxCount = FixedSize.ints.readImpl(inputStream);
      m.totalCount = FixedSize.ints.readImpl(inputStream);
      m.highestPosition = FixedSize.ints.readImpl(inputStream);
      return m;
    }
  }

  public static class PIndexWriter<K> implements Closeable {
    final PostingsConfig<K, PositionsCountMetadata, PositionsList> cfg;
    private final TemporaryDirectory tmpdir;
    TmpStreamPostingIndexWriter<K, PositionsCountMetadata, PositionsList> writer;
    PostingIndexWriter<K, PositionsCountMetadata, PositionsList> finalWriter;

    public PIndexWriter(Coder<K> keyCoder, Directory outdir) throws IOException {
      this(keyCoder, outdir, "positions");
    }
    public PIndexWriter(Coder<K> keyCoder, Directory outdir, String baseName) throws IOException {
      cfg = new PostingsConfig<>(
          keyCoder,
          new PositionsCountMetadataCoder(),
          new PositionsListCoder(),
          Comparing.defaultComparator(),
          new PositionsCountMetadata()
      );
      this.tmpdir = new TemporaryDirectory();
      this.writer = cfg.makeTemporaryWriter(tmpdir, baseName);
      this.finalWriter = cfg.makeFinalWriter(outdir, baseName);
    }

    public void add(K key, int document, PositionsList positions) {
      writer.add(key, document, positions);
    }

    @Override
    public void close() throws IOException {
      writer.mergeTo(finalWriter);
      writer.close();
      tmpdir.close();
      finalWriter.close();
    }
  }

  public static <K> IOMap<K, PostingMover<PositionsList>> openReader(Coder<K> keyCoder, Directory input) throws IOException {
    return openReader(keyCoder, input, "positions");
  }
  public static <K> IOMap<K, PostingMover<PositionsList>> openReader(Coder<K> keyCoder, Directory input, String baseName) throws IOException {
    PostingsConfig<K,PositionsCountMetadata,PositionsList> cfg = new PostingsConfig<>(
        keyCoder,
        new PositionsCountMetadataCoder(),
        new PositionsListCoder(),
        Comparing.defaultComparator(),
        new PositionsCountMetadata()
    );

    return cfg.openReader(input, baseName);
  }
}
