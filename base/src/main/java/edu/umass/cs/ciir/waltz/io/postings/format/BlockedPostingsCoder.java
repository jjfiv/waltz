package edu.umass.cs.ciir.waltz.io.postings.format;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.ints.IntsCoder;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;
import edu.umass.cs.ciir.waltz.dociter.movement.BlockPostingsMover;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.io.postings.AbstractPostingListCoder;
import edu.umass.cs.ciir.waltz.io.postings.AbstractValueBuilder;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * This is a coder that reads and writes high level {:link PostingMover} objects.
 * @param <V> the type of value to put in the posting list.
 */
public class BlockedPostingsCoder<V> extends AbstractPostingListCoder<V> {
  private final int blockSize;
  private final Coder<V> valCoder;
  private final IntsCoder intsCoder;

  public BlockedPostingsCoder(Coder<V> valCoder) throws IOException {
    this(BlockedPostingsFormat.DEFAULT_BLOCKSIZE, BlockedPostingsFormat.DEFAULT_INTSCODER, valCoder);
  }
  public BlockedPostingsCoder(int blockSize, IntsCoder intsCoder, Coder<V> valCoder) {
    this.blockSize = blockSize;
    assert(intsCoder.knowsOwnSize());
    assert(valCoder.knowsOwnSize());
    this.intsCoder = intsCoder;
    this.valCoder = valCoder;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(PostingMover<V> obj) throws IOException {
    AbstractValueBuilder<V> writer = new BlockedPostingValueBuilder<>(blockSize, intsCoder, valCoder);
    writer.add(obj);
    return writer.getOutput();
  }

  @Nonnull
  @Override
  public Class<?> getTargetClass() {
    return PostingMover.class;
  }

  @Nonnull
  @Override
  public PostingMover<V> read(StaticStream streamFn) throws IOException {
    return new BlockPostingsMover<>(new BlockedPostingsReader<>(intsCoder, valCoder, streamFn));
  }
}
