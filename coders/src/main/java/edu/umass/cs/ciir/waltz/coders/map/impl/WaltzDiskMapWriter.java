package edu.umass.cs.ciir.waltz.coders.map.impl;

import ciir.jfoley.chai.io.Directory;
import ciir.jfoley.chai.io.IO;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.files.DataSink;
import edu.umass.cs.ciir.waltz.coders.files.FileSink;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriter;
import edu.umass.cs.ciir.waltz.coders.sorter.ExternalSortingWriter;

import java.io.IOException;

import static edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMap.getKeyFileName;
import static edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMap.getKeySortDirectory;
import static edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMap.getValueFileName;

/**
 * @author jfoley
 */
public class WaltzDiskMapWriter<K, V> implements IOMapWriter<K, V> {
  final Directory outputDir;
  final FileSink valuesFile;

  final boolean sorting;
  final Directory sortDir;
  final ExternalSortingWriter<VocabEntry<K>> keySorter;

  final FileSink keysFile;
  final Coder<K> keyCoder;
  final Coder<V> valCoder;
  final VocabEntryCoder<K> koffCoder;
  long keyCount;

  public WaltzDiskMapWriter(Directory outputDir, String baseName, Coder<K> keyCoder, Coder<V> valCoder, boolean sorting) throws IOException {
    this.outputDir = outputDir;
    this.valuesFile = new FileSink(getValueFileName(outputDir, baseName));
    valuesFile.write(WaltzDiskMap.MagicCoder, "waltz.values");
    this.keysFile = new FileSink(getKeyFileName(outputDir, baseName));
    this.koffCoder = new VocabEntryCoder<>(keyCoder);
    this.keyCoder = keyCoder.lengthSafe();
    this.valCoder = valCoder; // valCoder need not be length-safe.
    this.sorting = sorting;
    keyCount = 0;

    if(sorting) {
      this.sortDir = new Directory(getKeySortDirectory(outputDir, baseName));
      this.keySorter = new ExternalSortingWriter<>(sortDir.get(), koffCoder);
    } else {
      this.sortDir = null;
      this.keySorter = null;

      // need to patch-up the keyCount at the end!
      keysFile.write(WaltzDiskMap.MagicCoder, "waltz.keys.naive");
      keysFile.write(FixedSize.longs, keyCount);
    }
  }

  public WaltzDiskMapWriter(Directory outputDir, String baseName, Coder<K> keyCoder, Coder<V> valCoder) throws IOException {
    this(outputDir, baseName, keyCoder, valCoder, true);
  }

  @Override
  public void put(K key, V val) throws IOException {
    beginWrite(key);
    valuesFile.write(valCoder, val);
    IO.close(val);
  }

  /**
   * Allows streaming building of this map; values can be written to immediately through the DataSink interface.
   *
   * @param key the key to associate with the data being written.
   * @throws IOException
   */
  public void beginWrite(K key) throws IOException {
    long startVal = valuesFile.tell();
    if(sorting) {
      keySorter.process(new VocabEntry<>(key, startVal));
    }
    keyCount++;
  }

  @Override
  public void putUnsafe(K key, DataChunk val) throws IOException {
    beginWrite(key);
    valuesFile.write(val);
    IO.close(val);
  }

  @Override
  public IOMapWriter<K, V> getSorting() throws IOException {
    return this;
  }

  @Override
  public void close() throws IOException {
    this.valuesFile.close();
    if(sorting) {
      this.keySorter.close();

      // TODO, replace this with a class/interface so you can store keys in many ways, and maybe a smart factory that chooses efficient implementations based on type.
      keysFile.write(WaltzDiskMap.MagicCoder, "waltz.keys.naive");
      keysFile.write(FixedSize.longs, keyCount);
      for (VocabEntry<K> kv : this.keySorter.getOutput()) {
        keysFile.write(koffCoder, kv);
      }
    } else {
      // Fix up the keyCount in the header:
      keysFile.seekAbsolute(WaltzDiskMap.MagicLength);
      keysFile.write(FixedSize.longs, keyCount);

      // clean up sort files:
      sortDir.removeRecursively();
    }
    keysFile.close();
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
    if(sorting) {
      keySorter.flush();
    }
    keysFile.flush();
    valuesFile.flush();
  }

  public DataSink valueWriter() {
    return this.valuesFile;
  }
}
