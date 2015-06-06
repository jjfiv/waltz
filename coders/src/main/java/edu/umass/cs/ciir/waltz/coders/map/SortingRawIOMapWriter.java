package edu.umass.cs.ciir.waltz.coders.map;

import ciir.jfoley.chai.IntMath;
import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.io.StreamFns;
import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.ByteArray;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.sorter.ExternalSortingWriter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley.
 */
public class SortingRawIOMapWriter implements RawIOMapWriter {
  private final TemporaryDirectory tmpdir;
  private final ExternalSortingWriter<DataChunkPair> sorter;
  private final RawIOMapWriter writer;

  public SortingRawIOMapWriter(RawIOMapWriter writer) throws IOException {
    this.tmpdir = new TemporaryDirectory();
    this.sorter = new ExternalSortingWriter<>(tmpdir.get(), new DataChunkPairCoder());
    this.writer = writer;
  }

  @Override
  public void put(DataChunk key, DataChunk val) throws IOException {
    sorter.process(new DataChunkPair(key, val));
  }

  @Override
  public void close() throws IOException {
    sorter.close();
    for (DataChunkPair dataChunkPair : sorter.getOutput()) {
      writer.put(dataChunkPair.getKey(), dataChunkPair.getValue());
    }
    writer.close();
    tmpdir.close();
  }

  @Override
  public RawIOMapWriter getSorting() {
    return this;
  }

  public static class DataChunkPair extends Pair<ByteArray,DataChunk> implements Comparable<DataChunkPair> {
    public DataChunkPair(DataChunk left, DataChunk right) {
      super(ByteArray.of(left), right);
    }

    @Override
    public int compareTo(@Nonnull DataChunkPair o) {
      return getKey().compareTo(o.getKey());
    }
  }
  public static class DataChunkPairCoder extends Coder<DataChunkPair> {
    public final Coder<Integer> sizeCoder = VarUInt.instance;
    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Nonnull
    @Override
    public DataChunk writeImpl(DataChunkPair obj) throws IOException {
      BufferList output = new BufferList();
      output.add(sizeCoder, IntMath.fromLong(obj.getKey().byteCount()));
      output.add(sizeCoder, IntMath.fromLong(obj.getValue().byteCount()));
      output.add(obj.getKey());
      output.add(obj.getValue());
      return output;
    }

    @Nonnull
    @Override
    public DataChunkPair readImpl(InputStream inputStream) throws IOException {
      int keySize = sizeCoder.readImpl(inputStream);
      int valSize = sizeCoder.readImpl(inputStream);
      ByteArray key = new ByteArray(StreamFns.readBytes(inputStream, keySize));
      ByteArray value = new ByteArray(StreamFns.readBytes(inputStream, valSize));
      return new DataChunkPair(key, value);
    }
  }
}
