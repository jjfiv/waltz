package edu.umass.cs.ciir.waltz.io.postings;

import ciir.jfoley.chai.lang.Builder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley
 */
public abstract class ValueBuilder<V> implements Builder<DataChunk>, Closeable {
  public abstract void add(int key, V value) throws IOException;
  @Override
  public abstract DataChunk getOutput();

  public void add(PostingMover<V> postings) throws IOException {
    for(; !postings.isDone(); postings.next()) {
      add(postings.currentKey(), postings.getCurrentPosting());
    }
  }
}
