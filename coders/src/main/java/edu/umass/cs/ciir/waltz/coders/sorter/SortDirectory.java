package edu.umass.cs.ciir.waltz.coders.sorter;

import edu.umass.cs.ciir.waltz.coders.Coder;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

/**
 * @author jfoley.
 */
public class SortDirectory<T> implements Iterable<T> {
  private final File dir;
  private final Comparator<? super T> cmp;
  private final Coder<Long> countCoder;
  private final Coder<T> itemCoder;

  public SortDirectory(File dir, Comparator<? super T> cmp, Coder<Long> countCoder, Coder<T> itemCoder) throws IOException {
    this.dir = dir;
    this.cmp = cmp;
    this.countCoder = countCoder;
    this.itemCoder = itemCoder;
  }

  @Override
  public MergingRunReader<T> iterator() {
    try {
      return MergingRunReader.openDirectory(dir, cmp, countCoder, itemCoder);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
