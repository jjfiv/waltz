package edu.umass.cs.ciir.waltz.coders.map.impl;

import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.collections.util.ListFns;
import ciir.jfoley.chai.io.Directory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.files.DataSource;
import edu.umass.cs.ciir.waltz.coders.files.FileChannelSource;
import edu.umass.cs.ciir.waltz.coders.files.FileSlice;
import edu.umass.cs.ciir.waltz.coders.map.IOMap;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMap.getKeyFileName;
import static edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMap.getKeySortDirectory;
import static edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMap.getValueFileName;

/**
 * @author jfoley
 */
public class WaltzDiskMapReader<K, V> implements IOMap<K, V> {
  final DataSource valuesFile;
  final Coder<V> valCoder;
  WaltzDiskMapVocabReader<K> vocab;

  public WaltzDiskMapReader(Directory baseDir, String baseName, Coder<K> keyCoder, Coder<V> valCoder) throws IOException {
    if (getKeySortDirectory(baseDir, baseName).exists()) {
      throw new IllegalStateException("Key sorting files still exist :(");
    }

    this.valCoder = valCoder;

    FileChannelSource keys = new FileChannelSource(getKeyFileName(baseDir, baseName));

    valuesFile = new FileChannelSource(getValueFileName(baseDir, baseName));
    vocab = WaltzDiskMap.createVocabReader(keys, keyCoder, Comparing.defaultComparator());
  }

  @Override
  public long keyCount() {
    return vocab.count();
  }

  @Nonnull
  @Override
  public Map<String, Object> getConfig() {
    return Collections.emptyMap();
  }

  @Override
  public V get(K key) throws IOException {
    StaticStream ss = getSource(key);
    if (ss == null) return null;
    return valCoder.read(ss);
  }

  @Nullable
  @Override
  public StaticStream getSource(K key) throws IOException {
    FileSlice kv = vocab.find(key);
    if (kv == null) return null;
    return valuesFile.getSource(kv.start, kv.size());
  }

  @Nonnull
  @Override
  public List<Pair<K, V>> getInBulk(List<K> keys) throws IOException {
    List<Pair<K, V>> vals = new ArrayList<>();


    List<Pair<K, FileSlice>> data = vocab.findInBulk(keys);

    for (Pair<K, FileSlice> pr : data) {
      K key = pr.left;
      FileSlice slice = pr.right;
      vals.add(Pair.of(key, get(slice)));
    }

    return vals;
  }

  @Nonnull
  private V get(FileSlice slice) throws IOException {
    return valCoder.read(valuesFile.getSource(slice.start, slice.size()));
  }

  @Nonnull
  @Override
  public Iterable<K> keys() throws IOException {
    return vocab.keys();
  }

  @Nonnull
  @Override
  public Iterable<Pair<K, V>> items() throws IOException {
    return ListFns.lazyMap(vocab.slices(), (kv) -> {
      try {
        return Pair.of(kv.left, get(kv.right));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public void close() throws IOException {
    vocab.close();
    valuesFile.close();
  }
}
