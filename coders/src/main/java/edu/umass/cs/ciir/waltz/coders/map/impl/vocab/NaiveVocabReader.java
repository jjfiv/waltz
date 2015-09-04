package edu.umass.cs.ciir.waltz.coders.map.impl.vocab;

import ciir.jfoley.chai.IntMath;
import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.list.AChaiList;
import ciir.jfoley.chai.collections.util.ListFns;
import edu.umass.cs.ciir.waltz.coders.files.DataSource;
import edu.umass.cs.ciir.waltz.coders.files.FileSlice;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapVocabReader;

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
  private final int count;
  private final DataSource file;
  ArrayList<VocabEntry<K>> keysAndOffsets;
  final VocabConfig<K> cfg;

  public NaiveVocabReader(int count, VocabConfig<K> cfg, DataSource dataSource) throws IOException {
    this.count = count;
    this.cfg = cfg;
    this.file = dataSource;

    readAllKeys();
  }

  private void readAllKeys() throws IOException {
    // TODO, be less memory-lazy here:
    keysAndOffsets = new ArrayList<>();
    InputStream is = file.stream();
    keysAndOffsets.ensureCapacity(IntMath.fromLong(count));
    for (int i = 0; i < count; i++) {
      K key = cfg.keyCoder.read(is);
      long start = FixedSize.longs.read(is);
      int size = VarUInt.instance.read(is);
      VocabEntry<K> entry = new VocabEntry<K>(key, start, size, cfg.cmp);
      keysAndOffsets.add(entry);
    }
  }

  FileSlice getSlice(int i) {
    return keysAndOffsets.get(i).slice();
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
    return this.cfg.cmp;
  }

  @Override
  public List<Pair<K, FileSlice>> findInBulk(List<K> keys) {
    ArrayList<Pair<K, FileSlice>> data = new ArrayList<>();
    data.ensureCapacity(keys.size());
    Collections.sort(keys, cfg.cmp);

    int start = 0;
    for (K key : keys) {
      int pos = Collections.binarySearch(
          ListFns.slice(keys(), start, count),
          key, cfg.cmp);

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
    int pos = Collections.binarySearch(keys, key, cfg.cmp);
    if(pos >= 0) {
      return getSlice(pos);
    }
    return null;
  }

  @Override
  public void close() throws IOException {
    keysAndOffsets.clear();
    keysAndOffsets = null;
    file.close();
  }
}
