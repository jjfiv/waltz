package edu.umass.cs.ciir.waltz.io.postings;

import edu.umass.cs.ciir.waltz.coders.data.ByteArray;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.CharsetCoders;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.map.AbstractRawIOMap;
import edu.umass.cs.ciir.waltz.coders.map.IOMapImpl;
import edu.umass.cs.ciir.waltz.coders.map.RawIOMapWriter;
import edu.umass.cs.ciir.waltz.coders.streams.ByteArrayStaticStream;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.feature.MoverFeature;
import edu.umass.cs.ciir.waltz.io.postings.format.PostingCoder;
import edu.umass.cs.ciir.waltz.io.postings.streaming.StreamingPostingBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author jfoley
 */
public class StreamingPostingBuilderTest {
  public static class FakeRawIOMapRW extends AbstractRawIOMap implements RawIOMapWriter {
    public final Map<ByteArray, ByteArray> data;

    private FakeRawIOMapRW(Map<ByteArray, ByteArray> data) {
      this.data = data;
    }

    public static FakeRawIOMapRW hashMap() {
      return new FakeRawIOMapRW(new HashMap<>());
    }

    @Override
    public void put(DataChunk key, DataChunk val) throws IOException {
      ByteArray prev = data.put(new ByteArray(key.asByteArray().clone()), ByteArray.of(val));
      assertEquals(ByteArray.of(val), data.get(ByteArray.of(key)));
      assertNotNull(get(CharsetCoders.utf8.writeImpl(CharsetCoders.utf8.read(ByteArray.of(key)))));
      assert(prev == null);
    }

    @Override
    public void close() throws IOException {
      // nothing.
    }

    @Override
    public long keyCount() {
      return data.size();
    }

    @Override
    public StaticStream get(DataChunk key) throws IOException {
      ByteArray val = data.get(ByteArray.of(key));
      if(val == null) return null;
      return new ByteArrayStaticStream(val.data);
    }

    @Override
    public Iterable<DataChunk> keys() throws IOException {
      return new ArrayList<>(data.keySet());
    }
  }

  @Test
  public void testIndex() throws IOException {
    FakeRawIOMapRW map = FakeRawIOMapRW.hashMap();
    try (StreamingPostingBuilder<String, Integer> builder = new StreamingPostingBuilder<>(
        CharsetCoders.utf8,
        VarUInt.instance,
        map)) {
      builder.add("the", 11, 11);
      builder.add("the", 2, 20);
      builder.add("foolish", 11, 39);
      for (int i = 0; i < 1000; i++) {
        builder.add("many", i, i*2);
      }
    }
    assertNotNull(map.get(CharsetCoders.utf8.writeImpl("the")));

    assertEquals(3, map.keyCount());

    IOMapImpl<String, PostingMover<Integer>> converted = new IOMapImpl<>(
        map,
        CharsetCoders.utf8,
        new PostingCoder<>(VarUInt.instance));

    assertNotNull(map.get(CharsetCoders.utf8.writeImpl("the")));

    Set<String> keys = new HashSet<>();
    for (ByteArray byteArray : map.data.keySet()) {
      keys.add(CharsetCoders.utf8.read(byteArray));
    }
    assertNotNull(converted.get("the"));
    assertNotNull(keys);

    MoverFeature<Integer> theCounts = new MoverFeature<>(converted.get("the"));
    assertEquals(20, theCounts.getFeature(2).intValue());
    assertNull(theCounts.getFeature(3));
    assertEquals(11, theCounts.getFeature(11).intValue());

    MoverFeature<Integer> fooCounts = new MoverFeature<>(converted.get("foolish"));
    assertEquals(39, fooCounts.getFeature(11).intValue());

    MoverFeature<Integer> manyCounts = new MoverFeature<>(converted.get("many"));
    for (int i = 0; i < 1000; i++) {
      assertEquals(i*2, manyCounts.getFeature(i).intValue());
    }

  }

}