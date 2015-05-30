package edu.umass.cs.ciir.waltz.coders.map;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import java.io.IOException;

/**
 * @author jfoley
 */
public class IOMapWriterImpl<K extends Comparable<K>,V> implements IOMapWriter<K,V> {
  final RawIOMapWriter rawWriter;
  final Coder<K> keyCoder;
  final Coder<V> valCoder;
  public IOMapWriterImpl(RawIOMapWriter writer, Coder<K> keyCoder, Coder<V> valCoder) {
    this.rawWriter = writer;
    this.keyCoder = keyCoder;
    this.valCoder = valCoder;
  }

  @Override
  public void put(K key, V val) {
    try {
      rawWriter.put(keyCoder.writeData(key), valCoder.writeData(val));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void putUnsafe(K key, DataChunk val) throws IOException {
    rawWriter.put(keyCoder.writeData(key), val);
  }

  @Override
  public IOMapWriter<K,V> getSorting() throws IOException {
    return new SortingIOMapWriter<>(this);
  }

  @Override
  public void close() throws IOException {
    rawWriter.close();
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
    // nothing for now.
  }
}
