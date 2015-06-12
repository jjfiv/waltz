package edu.umass.cs.ciir.waltz.coders.map.rawsort;

import ciir.jfoley.chai.IntMath;
import ciir.jfoley.chai.io.StreamFns;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.ByteArray;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley.
 */
public class DataChunkPairCoder extends Coder<DataChunkPair> {
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
    obj.getKey().close();
    obj.getValue().close();
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
