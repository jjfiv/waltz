package edu.umass.cs.ciir.waltz.sys.positions;

import ciir.jfoley.chai.io.Directory;
import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.sys.PostingIndexWriter;
import edu.umass.cs.ciir.waltz.sys.PostingsConfig;
import edu.umass.cs.ciir.waltz.sys.tmp.TmpStreamPostingIndexWriter;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley
 */
public class PIndexWriter<K, V> implements Closeable {
  final PostingsConfig<K, V> cfg;
  private final TemporaryDirectory tmpdir;
  TmpStreamPostingIndexWriter<K, V> writer;
  PostingIndexWriter<K, V> finalWriter;

  public PIndexWriter(PostingsConfig<K,V> cfg, Directory outdir, String baseName) throws IOException {
    this.cfg = cfg;
    this.tmpdir = new TemporaryDirectory();
    this.writer = cfg.makeTemporaryWriter(tmpdir, baseName);
    this.finalWriter = cfg.makeFinalWriter(outdir, baseName);
  }

  public void add(K key, int document, V posting) {
    writer.add(key, document, posting);
  }

  @Override
  public void close() throws IOException {
    writer.mergeTo(finalWriter);
    writer.close();
    tmpdir.close();
    finalWriter.close();
  }
}
