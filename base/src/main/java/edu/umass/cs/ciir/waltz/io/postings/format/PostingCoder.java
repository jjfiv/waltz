package edu.umass.cs.ciir.waltz.io.postings.format;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;
import edu.umass.cs.ciir.waltz.dociter.movement.BlockPostingsMover;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.io.postings.PostingListCoder;
import edu.umass.cs.ciir.waltz.io.postings.ValueBuilder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * This is a coder that reads and writes high level {:link PostingMover} objects.
 * @param <V> the type of value to put in the posting list.
 */
public class PostingCoder<V> extends PostingListCoder<V> {
  private final int blockSize;
  private final Coder<V> valCoder;
  private final Coder<List<Integer>> intsCoder;

  public PostingCoder(Coder<V> valCoder) throws IOException {
    this(SimplePostingListFormat.DEFAULT_BLOCKSIZE, SimplePostingListFormat.DEFAULT_INTSCODER, valCoder);
  }
  public PostingCoder(int blockSize, Coder<List<Integer>> intsCoder, Coder<V> valCoder) {
    this.blockSize = blockSize;
    assert(intsCoder.knowsOwnSize());
    assert(valCoder.knowsOwnSize());
    this.intsCoder = intsCoder;
    this.valCoder = valCoder;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(PostingMover<V> obj) throws IOException {
    ValueBuilder<V> writer = new PostingValueBuilder<>(blockSize, intsCoder, valCoder);
    writer.add(obj);
    return writer.getOutput();
  }

  @Nonnull
  @Override
  public PostingMover<V> read(StaticStream streamFn) throws IOException {
    return new BlockPostingsMover<>(new Reader<>(intsCoder, valCoder, streamFn));
  }
}
