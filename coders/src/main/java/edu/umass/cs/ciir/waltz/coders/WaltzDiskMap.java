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
import edu.umass.cs.ciir.waltz.coders.files.*;
import edu.umass.cs.ciir.waltz.coders.kinds.ASCII;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.map.IOMap;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriter;
import edu.umass.cs.ciir.waltz.coders.sorter.ExternalSortingWriter;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author jfoley
 */
public class WaltzDiskMap {
  public static final Coder<String> MagicCoder = new ASCII.FixedLength(128);

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
    public String toString() {
      return "<"+key+":"+offset+">";
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
      valuesFile.write(MagicCoder, "waltz.values");
      this.keysFile = new FileSink(outputDir.childPath(baseName+".keys"));
      this.koffCoder = new KeyToValueOffsetCoder<>(keyCoder);
      this.sortDir = outputDir.childDir(baseName + ".ksort");
      this.keySorter = new ExternalSortingWriter<>(sortDir.get(), koffCoder);
      this.keyCoder = keyCoder.lengthSafe();
      this.valCoder = valCoder; // valCoder need not be length-safe.
      keyCount = 0;
    }

    @Override
    public void put(K key, V val) throws IOException {
      beginWrite(key);
      valuesFile.write(valCoder, val);
      IO.close(val);
    }

    /**
     * Allows streaming building of this map; values can be written to immediately through the DataSink interface.
     * @param key the key to associate with the data being written.
     * @throws IOException
     */
    public void beginWrite(K key) throws IOException {
      long startVal = valuesFile.tell();
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
      keysFile.write(MagicCoder, "waltz.keys.naive");
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

    public DataSink valueWriter() {
      return this.valuesFile;
    }
  }

  public static class Reader<K,V> implements IOMap<K,V> {
    final DataSource valuesFile;
    final Coder<V> valCoder;
    Vocab<K> vocab;

    public Reader(Directory baseDir, String baseName, Coder<K> keyCoder, Coder<V> valCoder) throws IOException {
      if(baseDir.child(baseName+".ksort").exists()) {
        throw new IllegalStateException("Key sorting files still exist :(");
      }

      this.valCoder = valCoder;

      FileChannelSource keys = new FileChannelSource(baseDir.childPath(baseName+".keys"));

      valuesFile = new FileChannelSource(baseDir.childPath(baseName+".values"));
      vocab = createVocabReader(keys, valuesFile.size(), keyCoder, Comparing.defaultComparator());
    }

    @Override
    public long keyCount() {
      return vocab.count();
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

    @Nullable @Override
    public StaticStream getSource(K key) throws IOException {
      FileSlice kv = vocab.find(key);
      if(kv == null) return null;
      return valuesFile.getSource(kv.start, kv.size());
    }

    @Nonnull @Override
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

    @Nonnull @Override
    public Iterable<K> keys() throws IOException {
      return vocab.keys();
    }

    @Override
    public void close() throws IOException {
      vocab.close();
      valuesFile.close();
    }
  }

  public static <K> Vocab<K> createVocabReader(DataSource keys, long size, Coder<K> keyCoder, Comparator<K> cmp) throws IOException {
    DataSourceSkipInputStream stream = keys.stream();
    String magic = MagicCoder.read(stream);
    long count = 0;

    switch (magic) {
      case "waltz.keys.naive":
        count = FixedSize.longs.read(stream);
        break;
      default:
        throw new IOException("Unsupported keys format: "+magic);
    }

    // TODO dispatch better on type/count
    if(count < Integer.MAX_VALUE) {
      return new NaiveVocab<>(IntMath.fromLong(count), size, keyCoder, cmp, stream.sourceAtCurrentPosition());
    } else {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Galago called this part the "vocab" so I will too.
   * @param <K> the type of the keys.
   */
  public static interface Vocab<K> extends Closeable {
    @Nullable
    FileSlice find(K key) throws IOException;
    @Nonnull
    Iterable<K> keys() throws IOException;

    long count();
  }

  /**
   * Load it up into an arraylist and use binarySearch.
   * @param <K>
   */
  public static class NaiveVocab<K> implements Vocab<K> {
    final KeyToValueOffsetCoder<K> koffCoder;
    private final long maxValueSize;
    private final int count;
    ArrayList<KeyToValueOffset<K>> keysAndOffsets;
    final Comparator<K> keyCmp;
    final DataSource keys;

    public NaiveVocab(int count, long maxValueSize, Coder<K> keyCoder, Comparator<K> keyCmp, DataSource keys) throws IOException {
      this.count = count;
      this.keyCmp = keyCmp;
      this.keys = keys;
      this.koffCoder = new KeyToValueOffsetCoder<>(keyCoder);
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
}
