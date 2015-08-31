package edu.umass.cs.ciir.waltz.io.postings;

import edu.umass.cs.ciir.waltz.dociter.BlockPostingsIterator;
import edu.umass.cs.ciir.waltz.coders.streams.SkipInputStream;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;

import java.io.IOException;

/**
 * @author jfoley
 */
public abstract class StaticStreamPostingsIterator<X> implements BlockPostingsIterator<X> {
  protected SkipInputStream stream;
  protected final StaticStream streamSource;

  public StaticStreamPostingsIterator(StaticStream streamSource) {
    try {
      this.streamSource = streamSource;
      this.stream = streamSource.getNewStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Read the beginning of the stream to figure out what's going on */
  public abstract void readStreamHeader() throws IOException;

  @Override
  public void reset() {
    try {
      stream.close();
      stream = streamSource.getNewStream();
      readStreamHeader();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
