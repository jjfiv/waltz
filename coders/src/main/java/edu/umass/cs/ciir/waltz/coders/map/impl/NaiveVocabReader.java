package edu.umass.cs.ciir.waltz.coders.map.impl;

import ciir.jfoley.chai.IntMath;
import ciir.jfoley.chai.collections.Pair;
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

  FileSlice getSlice(int i) {
    long start = keysAndOffsets.get(i).offset;
    if(i+1 < keysAndOffsets.size()) {
      return new FileSlice(start, keysAndOffsets.get(i+1).offset);
    } else {
      return new FileSlice(start, maxValueSize);
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

  @Override
  public Comparator<K> keyComparator() {
    return this.keyCmp;
  }

  @Override
  public List<Pair<K, FileSlice>> findInBulk(List<K> keys) {
    ArrayList<Pair<K, FileSlice>> data = new ArrayList<>();
    data.ensureCapacity(keys.size());
    Collections.sort(keys, keyCmp);

    int start = 0;
    for (K key : keys) {
      int pos = Collections.binarySearch(
          ListFns.slice(keys(), start, count),
          key, keyCmp);

      //
      // Copied from the Collections.binarySearch javadoc:
      //
      // the index of the search key, if it is contained in the list;
      // otherwise, (-(insertion point) - 1).
      // The insertion point is defined as the point at which the key
      // would be inserted into the list: the index of the first element
      // greater than the key, or list.size() if all elements in the list
      // are less than the specified key. Note that this guarantees
      // that the return value will be >= 0 if and only if the key is found.
      //
      // rc = (-(insertion_point) -1)
      // rc + 1 = -insertion_point
      // insertion_point = (rc + 1) * -1
      // erego, we only need to search from [insertion_point, size()) the
      // next time around the loop.
      //

      if(pos < 0) {
        // insertion point is
        start = (pos+1) * -1;
        continue;
      }
      start = pos;
      data.add(Pair.of(key, getSlice(pos)));
    }

    return data;
  }

  @Override
  public List<Pair<K, FileSlice>> slices() {
    NaiveVocabReader<K> that = this;
    return new AChaiList<Pair<K, FileSlice>>() {
      @Override
      public Pair<K, FileSlice> get(int index) {
        return Pair.of(that.keysAndOffsets.get(index).key, getSlice(index));
      }

      @Override
      public int size() {
        return that.count;
      }
    };
  }

  @Nullable
  public FileSlice find(K key) throws IOException {
    List<K> keys = ListFns.lazyMap(keysAndOffsets, (x) -> x.key);
    int pos = Collections.binarySearch(keys, key, keyCmp);
    if(pos >= 0) {
      return getSlice(pos);
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
