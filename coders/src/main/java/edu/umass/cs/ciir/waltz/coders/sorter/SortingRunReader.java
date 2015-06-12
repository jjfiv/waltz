package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.io.IO;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.files.RunReader;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;

/**
 * Now this file mostly delegates to {@link RunReader}
 * @author jfoley.
 */
public class SortingRunReader<T> implements SortedReader<T> {
  private final RunReader<T> reader;
  private final Comparator<? super T> cmp;

  public SortingRunReader(Comparator<? super T> cmp, Coder<T> itemCoder, InputStream rawInputStream) throws IOException {
    this.cmp = cmp;
    this.reader = new RunReader<>(itemCoder, rawInputStream);
  }

  public SortingRunReader(Comparator<? super T> cmp, Coder<T> objCoder, File file) throws IOException {
    this(cmp, objCoder, IO.openInputStream(file));
  }

  @Override
  public T peek() { return reader.peek(); }
  @Override
  public boolean hasNext() { return reader.hasNext(); }
  @Override
  public T next() { return reader.next(); }
  public long getCount() { return reader.getCount(); }
  @Override
  public void close() throws IOException { reader.close(); }

  @Override
  public int compareTo(@Nonnull SortedReader<T> o) {
    return cmp.compare(peek(), o.peek());
  }
}
