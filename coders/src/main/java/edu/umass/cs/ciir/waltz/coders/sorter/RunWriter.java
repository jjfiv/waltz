package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.fn.SinkFn;
import ciir.jfoley.chai.io.IO;
import edu.umass.cs.ciir.waltz.coders.Coder;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author jfoley.
 */
public class RunWriter<T> implements SinkFn<T>, Closeable {
  private final OutputStream output;
  private final Coder<T> itemCoder;

  public RunWriter(long count, Coder<Long> countCoder, Coder<T> itemCoder, File output) throws IOException {
    this(count, countCoder, itemCoder, IO.openOutputStream(output));
  }

  public RunWriter(long count, Coder<Long> countCoder, Coder<T> itemCoder, OutputStream output) throws IOException {
    countCoder.write(output, count);
    this.output = output;
    this.itemCoder = itemCoder;
  }

  @Override
  public void close() throws IOException {
    output.close();
  }

  @Override
  public void process(T input) {
    try {
      itemCoder.write(output, input);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
