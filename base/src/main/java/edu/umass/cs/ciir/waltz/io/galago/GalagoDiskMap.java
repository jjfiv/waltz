package edu.umass.cs.ciir.waltz.io.galago;

import edu.umass.cs.ciir.waltz.io.Coder;
import edu.umass.cs.ciir.waltz.io.IOMap;
import edu.umass.cs.ciir.waltz.io.streams.SkipInputStream;
import edu.umass.cs.ciir.waltz.io.streams.StaticStream;
import org.lemurproject.galago.utility.CmpUtil;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.btree.disk.DiskBTreeIterator;
import org.lemurproject.galago.utility.btree.disk.DiskBTreeReader;
import org.lemurproject.galago.utility.btree.disk.DiskBTreeWriter;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

/**
 * @author jfoley
 */
public class GalagoDiskMap<K,V> implements IOMap<K,V> {

  private final Coder<K> keyCoder;
  private final Coder<V> valCoder;
  private final DiskBTreeReader reader;

  public static class Writer<K,V> implements Closeable {
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

  public static class SortingWriter<K,V> implements Closeable {
    List<DataChunkElement> bufferedElements = new ArrayList<>();
    private final Coder<K> keyCoder;
    private final Coder<V> valCoder;
    private final DiskBTreeWriter writer;

    public SortingWriter(Coder<K> keyCoder, Coder<V> valCoder, String fileName, Parameters argp) throws IOException {
      this.keyCoder = keyCoder;
      this.valCoder = valCoder;
      this.writer = new DiskBTreeWriter(fileName, argp);
    }

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

  public GalagoDiskMap(Coder<K> keyCoder, Coder<V> valCoder, String path) throws IOException {
    this.keyCoder = keyCoder;
    this.valCoder = valCoder;
    this.reader = new DiskBTreeReader(path);
  }

  @Override
  public long keyCount() {
    return reader.getManifest().getLong("keyCount");
  }

  @Override
  public Parameters getConfig() {
    return reader.getManifest();
  }

  @Override
  public V get(K key) throws IOException {
    StaticStream stream = getSource(key);
    if(stream == null) return null;
    return valCoder.read(stream);
  }

  @Override
  public SkipInputStream getStream(K key) throws IOException {
    StaticStream str = getSource(key);
    if(str == null) return null;
    return str.getNewStream();
  }

  @Override
  public StaticStream getSource(K key) throws IOException {
    byte[] kq = keyCoder.write(key).array();
    final DiskBTreeIterator iterator = reader.getIterator(kq);
    if(iterator == null) return null;
    return () -> {
      try {
        return new GalagoSkipInputStream(iterator.getValueStream());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };
  }

  @Override
  public Map<K,V> getInBulk(List<K> keys) throws IOException {
    List<byte[]> innerKeys = new ArrayList<>(keys.size());
    for (K key : keys) {
      innerKeys.add(keyCoder.write(key).array());
    }
    Collections.sort(innerKeys, new CmpUtil.ByteArrComparator());

    Map<K,V> output = new HashMap<>();
    DiskBTreeIterator iterator = reader.getIterator();
    for (int i = 0; !iterator.isDone() && i < innerKeys.size(); i++) {
      byte[] innerKey = innerKeys.get(i);
      iterator.skipTo(innerKey);
      byte[] currentKey = iterator.getKey();

      // not a match:
      if(currentKey == null || !Arrays.equals(currentKey, innerKey)) {
        continue;
      }

      // is a match:
      output.put(keyCoder.read(currentKey), valCoder.read(iterator.getValueStream()));
    }
    return output;
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }
}
