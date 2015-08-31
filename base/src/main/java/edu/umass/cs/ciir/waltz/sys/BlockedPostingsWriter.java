package edu.umass.cs.ciir.waltz.sys;

import ciir.jfoley.chai.io.Directory;
import edu.umass.cs.ciir.waltz.coders.files.DataSink;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapWriter;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.io.postings.format.BlockedPostingValueWriter;

import java.io.IOException;

/**
 * @author jfoley
 */
public class BlockedPostingsWriter<K, V> implements PostingIndexWriter<K, V> {
  public final WaltzDiskMapWriter<K, PostingMover<V>> writer;
  private final PostingsConfig<K, V> cfg;
  private BlockedPostingValueWriter<V> postingsWriter;
  DataSink valueWriter;

  public BlockedPostingsWriter(PostingsConfig<K, V> cfg, WaltzDiskMapWriter<K, PostingMover<V>> writer) {
    this.cfg = cfg;
    this.writer = writer;
    this.valueWriter = writer.valueWriter();
    this.postingsWriter = null;
  }

  public BlockedPostingsWriter(PostingsConfig<K, V> cfg, Directory outputDir, String baseName) throws IOException {
    this(cfg, new WaltzDiskMapWriter<>(outputDir, baseName, cfg.keyCoder, null, false));
  }

  @Override
  public void writeNewKey(K key) throws IOException {
    finishCurrentPostingList();
    writer.beginWrite(key);
  }

  @Override
  public void writeMetadata(KeyMetadata<V> metadata) throws IOException {
    assert (postingsWriter == null);
    valueWriter.write(metadata.encode());
    postingsWriter = new BlockedPostingValueWriter<>(valueWriter, cfg.blockSize, cfg.docsCoder, cfg.valCoder);
  }

  @Override
  public void writePosting(int doc, V value) throws IOException {
    assert (postingsWriter != null);
    postingsWriter.add(doc, value);
  }

  public void finishCurrentPostingList() throws IOException {
    if (postingsWriter != null) {
      postingsWriter.finish();
      postingsWriter = null;
    }
  }

  @Override
  public void close() throws IOException {
    finishCurrentPostingList();
    writer.close();
  }
}
