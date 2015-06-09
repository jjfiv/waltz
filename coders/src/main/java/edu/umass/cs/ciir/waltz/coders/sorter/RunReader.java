package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.io.StreamFns;
import edu.umass.cs.ciir.waltz.coders.Coder;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;

/**
 * @author jfoley.
 */
public class RunReader<T> implements Closeable, Comparable<RunReader<T>> {
  private final InputStream source;
  private final long count;
  private final Coder<T> itemCoder;
  private final Comparator<? super T> cmp;
  private T nextItem;
  private int index;

  public RunReader(Comparator<? super T> cmp, Coder<T> itemCoder, FileChannel source) throws IOException {
    this.cmp = cmp;
    this.itemCoder = itemCoder;
    // read prefixed, uncompressed length:
    this.count = RunWriter.countCoder.read(StreamFns.readChannel(source, 8));
    this.source = new GZIPInputStream(Channels.newInputStream(source));
    index = 0;
    next();
  }

  public RunReader(Comparator<? super T> cmp, Coder<T> objCoder, File file) throws IOException {
    this(cmp, objCoder, FileChannel.open(file.toPath(), StandardOpenOption.READ));
  }

  public T peek() {
    return nextItem;
  }

  public boolean hasNext() {
    return nextItem != null;
  }

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

  @Override
  public int compareTo(@Nonnull RunReader<T> o) {
    return cmp.compare(peek(), o.peek());
  }


  public long getCount() {
    return count;
  }
}
