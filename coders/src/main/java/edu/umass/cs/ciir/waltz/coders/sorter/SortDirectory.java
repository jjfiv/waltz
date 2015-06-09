package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.collections.iters.ClosingIterator;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.reduce.Reducer;
import edu.umass.cs.ciir.waltz.coders.reduce.ReducingIterator;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

/**
 * @author jfoley.
 */
public class SortDirectory<T> implements Iterable<T> {
  private final File dir;
  private final Comparator<? super T> cmp;
  private final Reducer<T> reducer;
  private final Coder<T> itemCoder;

  public SortDirectory(File dir, Comparator<? super T> cmp, Reducer<T> reducer, Coder<T> itemCoder) throws IOException {
    this.dir = dir;
    this.cmp = cmp;
    this.reducer = reducer;
    this.itemCoder = itemCoder;
  }

  @Override
  public ClosingIterator<T> iterator() {
    try {
      ClosingIterator<T> rawIterator = MergingRunReader.openDirectory(dir, cmp, itemCoder);
      return new ReducingIterator<>(reducer, rawIterator);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
