package edu.umass.cs.ciir.waltz.coders.map.impl;

import ciir.jfoley.chai.IntMath;
import ciir.jfoley.chai.collections.list.AChaiList;
import ciir.jfoley.chai.collections.util.ListFns;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.files.DataSource;
import edu.umass.cs.ciir.waltz.coders.files.FileSlice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Load it up into an arraylist and use binarySearch.
 * @param <K>
 */
public class NaiveVocabReader<K> implements WaltzDiskMapVocabReader<K> {
  final VocabEntryCoder<K> koffCoder;
  private final long maxValueSize;
  private final int count;
  ArrayList<VocabEntry<K>> keysAndOffsets;
  final Comparator<K> keyCmp;
  final DataSource keys;

  public NaiveVocabReader(int count, long maxValueSize, Coder<K> keyCoder, Comparator<K> keyCmp, DataSource keys) throws IOException {
    this.count = count;
    this.keyCmp = keyCmp;
    this.keys = keys;
    this.koffCoder = new VocabEntryCoder<>(keyCoder);
    this.maxValueSize = maxValueSize;

    // TODO, be less memory-lazy here:
    keysAndOffsets = new ArrayList<>();
    InputStream is = keys.stream();
    keysAndOffsets.ensureCapacity(IntMath.fromLong(count));
    for (int i = 0; i < count; i++) {
      keysAndOffsets.add(koffCoder.read(is));
    }
  }

  @Nonnull
  public AChaiList<K> keys() {
    return ListFns.lazyMap(keysAndOffsets, x -> x.key);
  }

  @Override
  public long count() {
    return count;
  }

  @Nullable
  public FileSlice find(K key) throws IOException {
    List<K> keys = ListFns.lazyMap(keysAndOffsets, (x) -> x.key);
    int pos = Collections.binarySearch(keys, key, keyCmp);
    if(pos >= 0) {
      long start = keysAndOffsets.get(pos).offset;
      if(pos+1 < keysAndOffsets.size()) {
        return new FileSlice(start, keysAndOffsets.get(pos+1).offset);
      } else {
        return new FileSlice(start, maxValueSize);
      }
    }
    return null;
  }

  @Override
  public void close() throws IOException {
    keysAndOffsets.clear();
    keysAndOffsets = null;
    keys.close();
  }
}
