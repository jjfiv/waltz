package edu.umass.cs.ciir.waltz.coders.map.impl;

import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.io.Directory;
import ciir.jfoley.chai.io.IO;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.files.DataSink;
import edu.umass.cs.ciir.waltz.coders.files.FileSink;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriter;
import edu.umass.cs.ciir.waltz.coders.map.impl.vocab.*;

import java.io.IOException;

import static edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMap.getKeyFileName;
import static edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMap.getValueFileName;

/**
 * @author jfoley
 */
public class WaltzDiskMapWriter<K, V> implements IOMapWriter<K, V> {
  final Directory outputDir;
  final FileSink valuesFile;

  VocabWriter<K> vocabWriter;
  final Coder<K> keyCoder;
  final Coder<V> valCoder;

  public WaltzDiskMapWriter(Directory outputDir, String baseName, Coder<K> keyCoder, Coder<V> valCoder) throws IOException {
    this.outputDir = outputDir;
    this.valuesFile = new FileSink(getValueFileName(outputDir, baseName));
    valuesFile.write(WaltzDiskMap.MagicCoder, "waltz.values");

    VocabConfig<K> config = new VocabConfig<>(
        keyCoder.lengthSafe(),
        Comparing.defaultComparator()
    );

    vocabWriter = new NaiveVocabWriter<>(new FileSink(getKeyFileName(outputDir, baseName)), config);
    vocabWriter.writeHeader();
    this.keyCoder = keyCoder.lengthSafe();
    this.valCoder = valCoder; // valCoder need not be length-safe.
  }

  @Override
  public void put(K key, V val) throws IOException {
    beginWrite(key);
    valuesFile.write(valCoder, val);
    finishWrite();
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
    vocabWriter.onKey(key, startVal);
  }

  public void finishWrite() throws IOException {
    vocabWriter.onFinishKey(valuesFile.tell());
  }

  @Override
  public void putUnsafe(K key, DataChunk val) throws IOException {
    beginWrite(key);
    valuesFile.write(val);
    finishWrite();
    IO.close(val);
  }

  @Override
  public IOMapWriter<K, V> getSorting() throws IOException {
    return this;
  }

  @Override
  public void close() throws IOException {
    this.valuesFile.close();
    vocabWriter.close();
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
  public void flush() throws IOException { }

  public DataSink valueWriter() {
    return this.valuesFile;
  }
}
