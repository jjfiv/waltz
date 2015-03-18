package edu.umass.cs.ciir.waltz.io.galago;

import ciir.jfoley.chai.collections.Pair;
import edu.umass.cs.ciir.waltz.io.Coder;
import edu.umass.cs.ciir.waltz.io.map.IOMap;
import edu.umass.cs.ciir.waltz.io.map.RawIOMap;
import edu.umass.cs.ciir.waltz.io.streams.StaticStream;
import edu.umass.cs.ciir.waltz.io.util.ByteArray;
import edu.umass.cs.ciir.waltz.io.util.DataChunk;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.btree.disk.DiskBTreeIterator;
import org.lemurproject.galago.utility.btree.disk.DiskBTreeReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author jfoley
 */
public class RawGalagoDiskMap implements RawIOMap {
  private final DiskBTreeReader reader;

  private RawGalagoDiskMap(DiskBTreeReader reader) {
    this.reader = reader;
  }
  private RawGalagoDiskMap(String path) throws IOException {
    this(new DiskBTreeReader(path));
  }

  public static <K,V> IOMap<K,V> createIOMap(Coder<K> keyCoder, Coder<V> valCoder, String path) throws IOException {
    return new IOMapImpl<>(create(path), keyCoder, valCoder);
  }
  public static RawIOMap create(String path) throws IOException {
    return new RawGalagoDiskMap(path);
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
  public StaticStream get(DataChunk key) throws IOException {
    byte[] kq = key.asByteArray();
    final DiskBTreeIterator iterator = reader.getIterator(kq);
    if(iterator == null) return null; // We ran off the end.
    if(!Arrays.equals(iterator.getKey(), kq)) return null; // We found something close, but not equal.
    return new BTreeIterStaticStream(iterator);
  }


  @Override
  public List<Pair<DataChunk, StaticStream>> getInBulk(List<DataChunk> keys) throws IOException {
    List<ByteArray> innerKeys = new ArrayList<>(keys.size());
    for (DataChunk key : keys) {
      innerKeys.add(new ByteArray(key.asByteArray()));
    }
    Collections.sort(innerKeys);

    List<Pair<DataChunk, StaticStream>> output = new ArrayList<>(keys.size());
    DiskBTreeIterator iterator = reader.getIterator();
    for (int i = 0; !iterator.isDone() && i < innerKeys.size(); i++) {
      byte[] innerKey = innerKeys.get(i).data;
      iterator.skipTo(innerKey);
      byte[] currentKey = iterator.getKey();

      // not a match:
      if(currentKey == null || !Arrays.equals(currentKey, innerKey)) {
        continue;
      }

      // is a match:
      output.add(Pair.of(new ByteArray(currentKey), new BTreeIterStaticStream(iterator)));
    }
    return output;
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

}
