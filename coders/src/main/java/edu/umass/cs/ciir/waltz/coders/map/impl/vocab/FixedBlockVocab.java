package edu.umass.cs.ciir.waltz.coders.map.impl.vocab;

import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.files.FileSink;
import edu.umass.cs.ciir.waltz.coders.files.FileSlice;
import edu.umass.cs.ciir.waltz.coders.files.WriteLongLater;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMap;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author jfoley
 */
public class FixedBlockVocab {

  public static class FixedBlockVocabWriter<K> implements Closeable {
    VocabConfig<K> config;
    public FileSink keysFile;
    public FileSink blocksFile;
    public int keysInBlock;
    public long keyCount;
    WriteLongLater keyCountUpdater;

    VocabFileSlice<K> currentKey = null;
    K lastKeyWritten = null;
    VocabBlockSlice<K> currentBlock = null;

    public void writeHeader() throws IOException {
      assert(keysInBlock >= 2);
      keysFile.write(WaltzDiskMap.MagicCoder, "waltz.keys.FixedBlockVocab");
      keysFile.write(FixedSize.ints, keysInBlock);
      keyCountUpdater = new WriteLongLater(keysFile);
    }

    public void beginKey(K key, long offset) {
      assert(currentKey == null);
      keyCount++;

    }

    public void endKey(long size) throws IOException {
      currentKey.size = size;
      process(currentKey.key, currentKey.start, currentKey.size);
      currentKey = null;
    }

    public void process(K key, long start, long size) throws IOException {
      int index = (int) (keyCount % keysInBlock);
      if(index == 0) {
        startBlock(key, keysFile.tell());
      }
      keysFile.write(config.keyCoder, key);
      keysFile.write(config.offsetCoder, start);
      keysFile.write(config.sizeCoder, size);
      lastKeyWritten = key;
    }

    private void startBlock(K key, long keyFileOffset) {
      assert(currentBlock == null);
    }

    @Override
    public void close() throws IOException {
      assert(currentKey == null);
      keyCountUpdater.write(keyCount);
    }
  }

  public static class VocabTreeWriter<K> {
    final VocabConfig<K> config;
    public final FileSink sink;
    K firstKey;
    K lastKey;
    long start;
    long size;

    public VocabTreeWriter(VocabConfig<K> config, FileSink sink) {
      this.config = config;
      this.sink = sink;
      firstKey = null;
      lastKey = null;
    }

    public void startBlock(K key, long keyFileOffset) {
      assert(firstKey == null);
      firstKey = key;
      start = keyFileOffset;
    }

    public void finishBlock(K lastKey, long afterOffset) throws IOException {
      size = afterOffset - start;
      this.lastKey = lastKey;

      sink.write(config.keyCoder, firstKey);
      sink.write(config.keyCoder, lastKey);
      sink.write(FixedSize.longs, start);
      sink.write(FixedSize.longs, size);

      firstKey = null;
    }

  }

  public static class VocabKeyBlockBuilder<K> {
    private final VocabConfig<K> config;
    public int count;
    ByteBuilder data = new ByteBuilder();

    public VocabKeyBlockBuilder(VocabConfig<K> config) {
      this.config = config;
    }

    public int getCount() { return count; }
    public void add(K key, long start, long size) {
      data.add(config.keyCoder, key);
      data.add(config.offsetCoder, start);
      data.add(config.sizeCoder, size);
    }
  }

  public interface IVocabEntryBlock<K> {
    VocabEntry<K> match(K query);
  }
  public static class VocabKeyBlock<K> {
    private ArrayList<K> keys;
    private long[] starts;
    private long[] ends;
    private VocabConfig<K> cfg;

    public VocabKeyBlock(VocabConfig<K> cfg, int size) {
      this.cfg = cfg;
      starts = new long[size];
      ends = new long[size];
      keys = new ArrayList<>(size);
      for (int i = 0; i < keys.size(); i++) {
        keys.add(null);
      }
      Arrays.fill(starts, -1);
      Arrays.fill(ends, -1);
    }

    public FileSlice match(K key) {
      int index = Collections.binarySearch(keys, key, cfg.cmp);
      if(index < 0) return null;
      return new FileSlice(starts[index], ends[index]);
    }
  }

  public static class VocabFileSlice<K> {
    K key;
    long start;
    long size;
  }

  public static class VocabBlockSlice<K> {
    K firstKey;
    K lastKey;
    long start;
    long size;
  }
}
