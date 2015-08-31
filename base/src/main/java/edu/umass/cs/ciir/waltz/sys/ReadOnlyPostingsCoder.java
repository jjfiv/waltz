package edu.umass.cs.ciir.waltz.sys;

import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;
import edu.umass.cs.ciir.waltz.dociter.movement.BlockPostingsMover;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.io.postings.AbstractPostingListCoder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Objects;

/**
 * @author jfoley
 */
public class ReadOnlyPostingsCoder<V> extends AbstractPostingListCoder<V> {
  private final PostingsConfig<?, V> cfg;

  public ReadOnlyPostingsCoder(PostingsConfig<?, V> cfg) {
    this.cfg = Objects.requireNonNull(cfg);
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(PostingMover<V> obj) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Nonnull
  @Override
  public PostingMover<V> read(StaticStream streamFn) throws IOException {
    return new BlockPostingsMover<>(new MetadataBlockPostingsIterator<>(cfg, streamFn));
  }
}
