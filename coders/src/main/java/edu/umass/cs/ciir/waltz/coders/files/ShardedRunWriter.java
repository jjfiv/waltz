package edu.umass.cs.ciir.waltz.coders.files;

import ciir.jfoley.chai.io.Directory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.reduce.ClosingSinkFn;

import java.io.IOException;

/**
 * A one to many RunWriter
 * @author jfoley
 */
public class ShardedRunWriter<T> implements ClosingSinkFn<T> {
  public final Directory runDirectory;
  private final long swapCount;
  private final Coder<T> itemCoder;
  private RunWriter<T> writer;
  private int nextIdentifier;

  public ShardedRunWriter(Coder<T> itemCoder, Directory runDirectory, long swapCount) throws IOException {
    this.runDirectory = runDirectory;
    this.itemCoder = itemCoder;
    nextIdentifier = 0;
    shiftToNextWriter();
    this.swapCount = swapCount;
    assert(swapCount >= 1);
    nextIdentifier = 1;
  }

  void shiftToNextWriter() throws IOException {
    System.err.println("ShardedTextWriter::shiftToNextWriter");
    int current = nextIdentifier++;
    if(writer != null) {
      writer.close();
    }
    writer = new RunWriter<>(itemCoder, runDirectory.child(current+".run"));
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }

  @Override
  public synchronized void process(T input) {
    if(writer.getCount() >= swapCount) {
      try {
        shiftToNextWriter();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    writer.process(input);
  }
}
