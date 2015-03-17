package edu.umass.cs.ciir.waltz.io.galago;

import edu.umass.cs.ciir.waltz.io.util.DataChunk;
import org.lemurproject.galago.utility.btree.IndexElement;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
* @author jfoley
*/
public class DataChunkElement implements IndexElement {
  private final DataChunk val;
  private final DataChunk key;

  public DataChunkElement(DataChunk key, DataChunk val) {
    this.key = key;
    this.val = val;
  }

  @Override
  public byte[] key() {
    return key.asByteBuffer().array();
  }

  @Override
  public long dataLength() {
    return val.byteCount();
  }

  @Override
  public void write(OutputStream stream) throws IOException {
    val.write(stream);
    if(val instanceof Closeable) {
      ((Closeable) val).close();
    }
  }
}
