package edu.umass.cs.ciir.waltz.galago.io;

import ciir.jfoley.chai.collections.Pair;
import edu.umass.cs.ciir.waltz.coders.data.ByteArray;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.map.RawIOMap;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.btree.BTreeIterator;
import org.lemurproject.galago.utility.btree.disk.DiskBTreeIterator;
import org.lemurproject.galago.utility.btree.disk.DiskBTreeReader;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

/**
 * @author jfoley
 */
public class RawGalagoDiskMap implements RawIOMap {
  private final DiskBTreeReader reader;

  RawGalagoDiskMap(DiskBTreeReader reader) {
    this.reader = reader;
  }
  RawGalagoDiskMap(String path) throws IOException {
    this(new DiskBTreeReader(path));
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
    return new ReadableBufferStaticStream(iterator);
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
      output.add(Pair.of(new ByteArray(currentKey), new ReadableBufferStaticStream(iterator)));
    }
    return output;
  }

  private static class KeyIterator implements Iterator<DataChunk> {
    private final BTreeIterator iter;
    private KeyIterator(@Nonnull BTreeIterator iter) {
      this.iter = iter;
    }

    @Override
    public boolean hasNext() {
      return !iter.isDone();
    }

    @Override
    public DataChunk next() {
      if(iter.isDone()) return null;
      byte[] data = iter.getKey();
      assert(data != null) : "shouldn't be null unless the iterator is used up, I think";
      DataChunk currentKey = new ByteArray(iter.getKey());
      try {
        iter.nextKey();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return currentKey;
    }
  }

  @Override
  public Iterable<DataChunk> keys() throws IOException {
    return () -> {
      try {
        DiskBTreeIterator iterator = reader.getIterator();
        if(iterator == null) return Collections.<DataChunk>emptyList().iterator();
        return new KeyIterator(iterator);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

}
