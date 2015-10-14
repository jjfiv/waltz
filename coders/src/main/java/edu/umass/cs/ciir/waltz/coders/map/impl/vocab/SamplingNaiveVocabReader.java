package edu.umass.cs.ciir.waltz.coders.map.impl.vocab;

import ciir.jfoley.chai.IntMath;
import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.util.IterableFns;
import ciir.jfoley.chai.collections.util.MapFns;
import ciir.jfoley.chai.io.Directory;
import ciir.jfoley.chai.io.IO;
import ciir.jfoley.chai.io.StreamFns;
import edu.umass.cs.ciir.waltz.coders.CoderException;
import edu.umass.cs.ciir.waltz.coders.files.DataSource;
import edu.umass.cs.ciir.waltz.coders.files.DataSourceSkipInputStream;
import edu.umass.cs.ciir.waltz.coders.files.FileChannelSource;
import edu.umass.cs.ciir.waltz.coders.files.FileSlice;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapVocabReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * @author jfoley
 */
public class SamplingNaiveVocabReader<K> implements WaltzDiskMapVocabReader<K> {
  private final long count;
  private final DataSource file;
  ArrayList<VocabEntry<K>> keysAndOffsets;
  final VocabConfig<K> cfg;
  int sampleRate;
  ArrayList<K> sampledKeys;
  long[] sampledKeyPosition;

  public static boolean isUint(long val) {
    return val >= 0 && val <= Integer.MAX_VALUE;
  }

  public SamplingNaiveVocabReader(Directory baseDir, String baseName, long count, VocabConfig<K> cfg, DataSource dataSource) throws IOException {
    this.count = count;
    this.cfg = cfg;
    this.file = dataSource;

    File sampledKeysFile = baseDir.child(baseName+".sampled_keys");

    // fast, cached to file
    if(sampledKeysFile.exists()) {
      FileChannelSource skeys = new FileChannelSource(sampledKeysFile.getPath());
      DataSourceSkipInputStream stream = skeys.stream();
      sampleRate = FixedSize.ints.read(stream);
      int sampledKeyCount = FixedSize.ints.read(stream);

      sampledKeyPosition = new long[sampledKeyCount];
      ByteBuffer buf = ByteBuffer.wrap(StreamFns.readBytes(stream, sampledKeyCount * 8));
      for (int i = 0; i < sampledKeyCount; i++) {
        sampledKeyPosition[i] = buf.getLong(i*8);
      }

      sampledKeys = new ArrayList<>(sampledKeyCount);
      for (int i = 0; i < sampledKeyCount; i++) {
        sampledKeys.add(cfg.keyCoder.read(stream));
      }

      return;
    }

    for (int i = 5; i < 13; i++) {
      if(isUint(count >>> i)) {
        sampleRate = 1 << i;
      }
    }
    if(sampleRate == 0) throw new RuntimeException("Couldn't downsample the keyspace enough :(");
    int sampledKeyCount = IntMath.fromLong((long) Math.ceil(count / (double) sampleRate));

    sampledKeyPosition = new long[sampledKeyCount];
    sampledKeys = new ArrayList<>(sampledKeyCount);

    readAllKeys();
    saveSampledKeys(sampledKeysFile, sampledKeyCount);
  }

  private void saveSampledKeys(File sampledKeysFile, int sampledKeyCount) throws IOException {
    try (OutputStream out = IO.openOutputStream(sampledKeysFile)) {
      FixedSize.ints.write(out, sampleRate);
      FixedSize.ints.write(out, sampledKeyCount);
      for (long l : sampledKeyPosition) {
        FixedSize.longs.write(out, l);
      }
      for (K sampledKey : sampledKeys) {
        cfg.keyCoder.write(out, sampledKey);
      }
    }
  }

  private void readAllKeys() throws IOException {
    // TODO, be less memory-lazy here:
    keysAndOffsets = new ArrayList<>();
    DataSourceSkipInputStream is = file.stream();
    keysAndOffsets.ensureCapacity(IntMath.fromLong(count));

    int sampleIndex = 0;
    for (long i = 0; i < count; i++) {
      long position = is.tell();
      K key = cfg.keyCoder.read(is);
      is.skip(8); // long start = FixedSize.longs.read(is);
      int size = VarUInt.instance.read(is);
      if(i % sampleRate == 0) {
        sampledKeyPosition[sampleIndex++] = position;
        sampledKeys.add(key);
      }
    }
  }

  @Nonnull
  public Iterable<K> keys() {
    return IterableFns.map(slices(), Pair::getKey);
  }

  @Override
  public long count() {
    return count;
  }

  @Override
  public Comparator<K> keyComparator() {
    return this.cfg.cmp;
  }

  DataSourceSkipInputStream streamForBlock(int index) {
    try {
      return file.stream(this.sampledKeyPosition[index]);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Pair<K, FileSlice>> findInBulk(List<K> keys) {
    ArrayList<Pair<K, FileSlice>> data = new ArrayList<>(keys.size());

    TreeMap<Integer, List<K>> keysInBlock = new TreeMap<>();
    for (K key : keys) {
      MapFns.extendListInMap(keysInBlock, this.findSampledBlock(key), key);
    }

    for (Map.Entry<Integer, List<K>> kv : keysInBlock.entrySet()) {
      int block = kv.getKey();
      ArrayList<K> toFind = new ArrayList<>(kv.getValue());
      Collections.sort(toFind, cfg.cmp);

      int found = 0;
      DataSourceSkipInputStream is = streamForBlock(block);

      long startPosition = sampleRate*block;
      long amountLeft = count - startPosition;
      int itemsInBlock = (int) Math.min(amountLeft, sampleRate);

      // for key in block
      for (int i = 0; i < itemsInBlock && found < toFind.size(); i++) {
        try {
          K candidate = cfg.keyCoder.read(is);
          long start = FixedSize.longs.read(is);
          int size = VarUInt.instance.read(is);
          int cmp = cfg.cmp.compare(toFind.get(found), candidate);
          if (cmp == 0) {
            data.add(Pair.of(toFind.get(found++), new FileSlice(start, start + size)));
          } else if (cmp < 0) break;
        } catch (CoderException err) {
          System.err.println("i: "+i+" sampleRate: "+sampleRate+" found: "+found+" toFind.get(found): "+toFind.get(found)+" :"+toFind);
          throw err;
        }
      }
      //missing: found..toFind.size();
    }

    return data;
  }

  @Override
  public Iterable<Pair<K, FileSlice>> slices() {
    long count = this.count;
    DataSourceSkipInputStream is;
    try {
      is = file.stream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return () -> new Iterator<Pair<K,FileSlice>>() {
      long index = 0;

      @Override
      public boolean hasNext() {
        return index < count;
      }

      @Override
      public Pair<K, FileSlice> next() {
        index++;
        if(index > count) return null;
        K candidate = cfg.keyCoder.read(is);
        long start = FixedSize.longs.read(is);
        int size = VarUInt.instance.read(is);
        return Pair.of(candidate, new FileSlice(start, start+size));
      }
    };
  }

  private int findSampledBlock(K key) {
    int off = Collections.binarySearch(this.sampledKeys, key, cfg.cmp);
    if(off >= 0) return off;
    int index = (((off+1) * -1) -1);
    return index;
  }

  @Nullable
  public FileSlice find(K key) throws IOException {
    int block = findSampledBlock(key);
    DataSourceSkipInputStream is = file.stream(this.sampledKeyPosition[block]);


    long startPosition = sampleRate*block;
    long amountLeft = count - startPosition;
    int itemsInBlock = (int) Math.min(amountLeft, sampleRate);

    // todo cache block?
    for (int i = 0; i < itemsInBlock; i++) {
      K candidate = cfg.keyCoder.read(is);
      long start = FixedSize.longs.read(is);
      int size = VarUInt.instance.read(is);
      int cmp = cfg.cmp.compare(key, candidate);
      if(cmp == 0) {
        return new FileSlice(start, start+size);
      } else if(cmp < 0) break;
    }
    return null;
  }

  @Override
  public void close() throws IOException {
    file.close();
  }
}
