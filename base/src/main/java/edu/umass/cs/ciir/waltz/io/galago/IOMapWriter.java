package edu.umass.cs.ciir.waltz.io.galago;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.io.map.RawIOMapWriter;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley
 */
public class IOMapWriter<K,V> implements Closeable {
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


  @Override
  public void close() throws IOException {
    rawWriter.close();
  }
}
