package edu.umass.cs.ciir.waltz.io.coders;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class BytesCoder extends Coder<DataChunk> {
  private final int prefix;

  public BytesCoder(int prefix) {
    this.prefix = prefix;
  }
  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Override
  public DataChunk writeImpl(DataChunk obj) throws IOException {
    BufferList bl = new BufferList();
    bl.add(VByteCoders.ints, prefix);
    bl.add(VByteCoders.longs, obj.byteCount());
    bl.add(obj);
    return bl;
  }

  @Override
  public DataChunk readImpl(InputStream inputStream) throws IOException {
    int foundPrefix = VByteCoders.ints.read(inputStream);
    assert(foundPrefix == prefix);

    return null;
  }
}
