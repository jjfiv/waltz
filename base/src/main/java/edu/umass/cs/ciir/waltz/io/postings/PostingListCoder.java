package edu.umass.cs.ciir.waltz.io.postings;

import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.io.Coder;
import edu.umass.cs.ciir.waltz.io.streams.SkipInputStream;
import edu.umass.cs.ciir.waltz.io.streams.StaticStream;
import edu.umass.cs.ciir.waltz.io.util.DataChunk;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public abstract class PostingListCoder<V> extends Coder<PostingMover<V>> {
  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Override
  public abstract DataChunk writeImpl(PostingMover<V> obj) throws IOException;

  @Override
  public PostingMover<V> readImpl(InputStream inputStream) throws IOException {
    return read(() -> {
      return SkipInputStream.wrap(inputStream);
    });
  }
  @Override
  public abstract PostingMover<V> read(StaticStream streamFn) throws IOException;
}
