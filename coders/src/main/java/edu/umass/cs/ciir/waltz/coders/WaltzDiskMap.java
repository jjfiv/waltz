package edu.umass.cs.ciir.waltz.coders;

import ciir.jfoley.chai.IntMath;
import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.list.AChaiList;
import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.collections.util.ListFns;
import ciir.jfoley.chai.io.Directory;
import ciir.jfoley.chai.io.IO;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.files.DataSource;
import edu.umass.cs.ciir.waltz.coders.files.FileChannelSource;
import edu.umass.cs.ciir.waltz.coders.files.FileSink;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.map.IOMap;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriter;
import edu.umass.cs.ciir.waltz.coders.sorter.ExternalSortingWriter;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author jfoley
 */
public class WaltzDiskMap {
  public static class KeyToValueOffsetCoder<K> extends Coder<KeyToValueOffset<K>> {
    private final Coder<K> keyCoder;
    private final Coder<Long> offsetCoder;

    public KeyToValueOffsetCoder(Coder<K> keyCoder) {
      this(keyCoder, FixedSize.longs);
    }
    public KeyToValueOffsetCoder(Coder<K> keyCoder, Coder<Long> offsetCoder) {
      this.offsetCoder = offsetCoder;
      this.keyCoder = keyCoder.lengthSafe();
    }

    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Nonnull
    @Override
    public DataChunk writeImpl(KeyToValueOffset<K> obj) throws IOException {
      BufferList output = new BufferList();
      output.add(keyCoder, obj.key);
      output.add(offsetCoder, obj.offset);
      return output;
    }

    @Nonnull
    @Override
    public KeyToValueOffset<K> readImpl(InputStream inputStream) throws IOException {
      K key = keyCoder.readImpl(inputStream);
      long offset = offsetCoder.readImpl(inputStream);
      return new KeyToValueOffset<>(key, offset);
    }
  }
  public static class KeyToValueOffset<K> implements Comparable<KeyToValueOffset<K>> {
    public final K key;
    public final long offset;
    private final Comparator<K> cmp;

    public KeyToValueOffset(K key, long offset) {
      this(key, offset, Comparing.defaultComparator());
    }
    public KeyToValueOffset(K key, long offset, Comparator<K> keyCmp) {
      this.key = key;
      this.offset = offset;
      this.cmp = keyCmp;
    }

    @Override
    public int compareTo(@Nonnull KeyToValueOffset<K> o) {
      return cmp.compare(key, o.key);
    }
  }

  public static class Writer<K,V> implements IOMapWriter<K,V> {
    final Directory outputDir;
    final Directory sortDir;
    final FileSink valuesFile;
    final ExternalSortingWriter<KeyToValueOffset<K>> keySorter;
    final FileSink keysFile;
    final Coder<K> keyCoder;
    final Coder<V> valCoder;
    final KeyToValueOffsetCoder<K> koffCoder;
    long keyCount;

    public Writer(Directory outputDir, String baseName, Coder<K> keyCoder, Coder<V> valCoder) throws IOException {
      this.outputDir = outputDir;
      this.valuesFile = new FileSink(outputDir.childPath(baseName+".values"));
      this.keysFile = new FileSink(outputDir.childPath(baseName+".keys"));
      this.koffCoder = new KeyToValueOffsetCoder<>(keyCoder);
      this.sortDir = outputDir.childDir(baseName + ".ksort");
      this.keySorter = new ExternalSortingWriter<>(sortDir.get(), koffCoder);
      this.keyCoder = keyCoder.lengthSafe();
      this.valCoder = valCoder.lengthSafe();
      keyCount = 0;
    }

    @Override
    public void put(K key, V val) throws IOException {
      long startVal = valuesFile.tell();
      valuesFile.write(valCoder, val);
      IO.close(val);
      keySorter.process(new KeyToValueOffset<>(key, startVal));
      keyCount++;
    }

    @Override
    public void putUnsafe(K key, DataChunk val) throws IOException {
      long startVal = valuesFile.tell();
      valuesFile.write(val);
      IO.close(val);
      keySorter.process(new KeyToValueOffset<>(key, startVal));
      keyCount++;
    }

    @Override
    public IOMapWriter<K, V> getSorting() throws IOException {
      return this;
    }

    @Override
    public void close() throws IOException {
      this.valuesFile.close();
      this.keySorter.close();

      // TODO, replace this with a class/interface so you can store keys in many ways, and maybe a smart factory that chooses efficient implementations based on type.
      keysFile.write(FixedSize.longs, keyCount);
      for (KeyToValueOffset<K> kv : this.keySorter.getOutput()) {
        keysFile.write(koffCoder, kv);
      }
      keysFile.close();

      // clean up sort files:
      sortDir.removeRecursively();
    }

    @Override
    public Coder<K> getKeyCoder() {
      return keyCoder;
    }

    @Override
    public Coder<V> getValueCoder() {
      return valCoder;
    }

    @Override
    public void flush() throws IOException {
      keySorter.flush();
      keysFile.flush();
      valuesFile.flush();
    }

  }

  public static class FileSlice {
    /** inclusive */
    public final long start;
    /** exclusive */
    public final long end;

    public FileSlice(long start, long end) {
      assert(end > start);
      this.start = start;
      this.end = end;
    }
    public int size() {
      return IntMath.fromLong(end - start);
    }
  }

  public static class Reader<K,V> implements IOMap<K,V> {
    final DataSource valuesFile;
    final long count;
    final Coder<V> valCoder;
    final KeyToValueOffsetCoder<K> koffCoder;
    List<KeyToValueOffset<K>> keysAndOffsets;
    final Comparator<K> keyCmp;

    public Reader(Directory baseDir, String baseName, Coder<K> keyCoder, Coder<V> valCoder) throws IOException {
      if(baseDir.child(baseName+".ksort").exists()) {
        throw new IllegalStateException("Key sorting files still exist :(");
      }

      this.keyCmp = Comparing.defaultComparator();
      this.valCoder = valCoder;
      this.koffCoder = new KeyToValueOffsetCoder<>(keyCoder);

      // TODO, be less memory-lazy here:
      try (InputStream is = new FileInputStream(baseDir.childPath(baseName+".keys"))) {
        count = FixedSize.longs.read(is);
        for (long i = 0; i < count; i++) {
          keysAndOffsets.add(koffCoder.read(is));
        }
      }

      valuesFile = new FileChannelSource(baseDir.childPath(baseName+".values"));

    }

    @Override
    public long keyCount() {
      return count;
    }

    @Nonnull @Override
    public Map<String, Object> getConfig() {
      return Collections.emptyMap();
    }

    @Override
    public V get(K key) throws IOException {
      StaticStream ss = getSource(key);
      if(ss == null) return null;
      return valCoder.read(ss);
    }

    @Nullable
    private FileSlice find(K key) throws IOException {
      List<K> keys = ListFns.lazyMap(keysAndOffsets, (x) -> x.key);
      int pos = Collections.binarySearch(keys, key, keyCmp);
      if(pos >= 0) {
        long start = keysAndOffsets.get(pos).offset;
        if(pos+1 < keysAndOffsets.size()) {
          return new FileSlice(start, keysAndOffsets.get(pos+1).offset);
        } else {
          return new FileSlice(start, valuesFile.size());
        }
      }
      return null;
    }

    @Nullable @Override
    public StaticStream getSource(K key) throws IOException {
      FileSlice kv = find(key);
      if(kv == null) return null;
      return this.valuesFile.getSource(kv.start, kv.size());
    }

    @Nonnull
    @Override
    public List<Pair<K, V>> getInBulk(List<K> keys) throws IOException {
      List<Pair<K,V>> vals = new ArrayList<>();

      for (K key : keys) {
        V val = get(key);
        if(val != null) {
          vals.add(Pair.of(key, val));
        }
      }

      return vals;
    }

    @Nonnull
    @Override
    public AChaiList<K> keys() throws IOException {
      return ListFns.lazyMap(keysAndOffsets, (x) -> x.key);
    }

    @Override
    public void close() throws IOException {
      keysAndOffsets.clear();
      keysAndOffsets = null;
      valuesFile.close();
    }
  }
}
