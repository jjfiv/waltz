package edu.umass.cs.ciir.waltz.sys.positions;

import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.io.Directory;
import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.io.postings.PositionsListCoder;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.sys.PostingIndexWriter;
import edu.umass.cs.ciir.waltz.sys.PostingsConfig;
import edu.umass.cs.ciir.waltz.sys.tmp.TmpStreamPostingIndexWriter;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley
 */
public class PIndexWriter<K> implements Closeable {
  final PostingsConfig<K, PositionsList> cfg;
  private final TemporaryDirectory tmpdir;
  TmpStreamPostingIndexWriter<K, PositionsList> writer;
  PostingIndexWriter<K, PositionsList> finalWriter;

  public PIndexWriter(Coder<K> keyCoder, Directory outdir) throws IOException {
    this(keyCoder, outdir, "positions");
  }

  public PIndexWriter(Coder<K> keyCoder, Directory outdir, String baseName) throws IOException {
    cfg = new PostingsConfig<>(
        keyCoder,
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
