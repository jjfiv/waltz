package edu.umass.cs.ciir.waltz.io.postings;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.streams.SkipInputStream;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;

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
		return read(SkipInputStream.wrap(inputStream));
  }

  @Override
  public abstract PostingMover<V> read(StaticStream streamFn) throws IOException;
}
