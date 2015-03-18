package edu.umass.cs.ciir.waltz.io.galago;

import edu.umass.cs.ciir.waltz.io.Coder;
import edu.umass.cs.ciir.waltz.io.map.IOMapWriter;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.btree.disk.DiskBTreeWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jfoley
 */
public class GalagoDiskMap {
  public static class Writer<K,V> implements IOMapWriter<K,V> {
    private final Coder<K> keyCoder;
    private final Coder<V> valCoder;
    private final DiskBTreeWriter writer;

    public Writer(Coder<K> keyCoder, Coder<V> valCoder, String fileName, Parameters argp) throws IOException {
      this.keyCoder = keyCoder;
      this.valCoder = valCoder;
      this.writer = new DiskBTreeWriter(fileName, argp);
    }

    /** Requires keys be given in sorted order. */
    public void put(K key, V val) throws IOException {
      writer.add(new DataChunkElement(
          keyCoder.writeData(key),
          valCoder.writeData(val)
      ));
    }

    @Override
    public void close() throws IOException {
      writer.close();
    }
  }
  public static class SortingWriter<K,V> implements IOMapWriter<K,V> {
    List<DataChunkElement> bufferedElements = new ArrayList<>();
    private final Coder<K> keyCoder;
    private final Coder<V> valCoder;
    private final DiskBTreeWriter writer;

    public SortingWriter(Coder<K> keyCoder, Coder<V> valCoder, String fileName, Parameters argp) throws IOException {
      this.keyCoder = keyCoder;
      this.valCoder = valCoder;
      this.writer = new DiskBTreeWriter(fileName, argp);
    }

    @Override
    public void put(K key, V val) throws IOException {
      bufferedElements.add(new DataChunkElement(
          keyCoder.writeData(key),
          valCoder.writeData(val)
      ));
    }

    @Override
    public void close() throws IOException {
      Collections.sort(bufferedElements);
      for (DataChunkElement bufferedElement : bufferedElements) {
        writer.add(bufferedElement);
      }
      bufferedElements.clear();
    }
  }

  public static <A,B> IOMapWriter<A,B> getWriter(Coder<A> keyCoder, Coder<B> valCoder, String fileName) throws IOException {
    return new Writer<>(keyCoder, valCoder, fileName, Parameters.create());
  }
  public static <A,B> IOMapWriter<A,B> getSortingWriter(Coder<A> keyCoder, Coder<B> valCoder, String fileName) throws IOException {
    return new SortingWriter<>(keyCoder, valCoder, fileName, Parameters.create());
  }

}
