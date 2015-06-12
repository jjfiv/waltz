package edu.umass.cs.ciir.waltz.coders.files;

import ciir.jfoley.chai.collections.iters.ClosingIterator;
import ciir.jfoley.chai.io.IO;
import ciir.jfoley.chai.io.StreamFns;
import edu.umass.cs.ciir.waltz.coders.Coder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author jfoley
 */
public class RunReader<T> implements ClosingIterator<T> {
  private final InputStream source;
  private final long count;
  private final Coder<T> itemCoder;
  private T nextItem;
  private int index;

  public RunReader(Coder<T> itemCoder, InputStream rawInputStream) throws IOException {
    this.itemCoder = itemCoder;
    // read prefixed, uncompressed length:
    this.count = RunWriter.countCoder.read(StreamFns.readBytes(rawInputStream, 8));
    this.source = new GZIPInputStream(rawInputStream);
    index = 0;
    next();
  }

  public RunReader(Coder<T> objCoder, File file) throws IOException {
    this(objCoder, IO.openInputStream(file));
  }

  public T peek() {
    return nextItem;
  }

  @Override
  public boolean hasNext() {
    return nextItem != null;
  }

  @Override
  public T next() {
    T lastItem = nextItem;
    if (index < count) {
      this.nextItem = itemCoder.read(source);
      this.index++;
    } else {
      nextItem = null;
    }
    return lastItem;
  }

  @Override
  public void close() throws IOException {
    source.close();
  }

  public long getCount() {
    return count;
  }
}
