package edu.umass.cs.ciir.waltz.coders.map;

import edu.umass.cs.ciir.waltz.coders.Coder;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley
 */
public class IOMapWriter<K extends Comparable<K>,V> implements Closeable {
  final RawIOMapWriter rawWriter;
  final Coder<K> keyCoder;
  final Coder<V> valCoder;
  public IOMapWriter(RawIOMapWriter writer, Coder<K> keyCoder, Coder<V> valCoder) {
    this.rawWriter = writer;
    this.keyCoder = keyCoder;
    this.valCoder = valCoder;
  }

  public void put(K key, V val) {
    try {
      rawWriter.put(keyCoder.writeData(key), valCoder.writeData(val));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public SortingIOMapWriter<K,V> getSorting() throws IOException {
    return new SortingIOMapWriter<>(this);
  }

  @Override
  public void close() throws IOException {
    rawWriter.close();
  }
}
