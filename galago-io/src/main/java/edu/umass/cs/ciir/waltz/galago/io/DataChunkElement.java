package edu.umass.cs.ciir.waltz.galago.io;

import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import org.lemurproject.galago.utility.CmpUtil;
import org.lemurproject.galago.utility.btree.IndexElement;

import java.io.IOException;
import java.io.OutputStream;

/**
* @author jfoley
*/
public class DataChunkElement implements IndexElement, Comparable<DataChunkElement> {
  private final byte[] key;
  private final DataChunk val;

  public DataChunkElement(DataChunk key, DataChunk val) {
    this.key = key.asByteBuffer().array();
    try {
      key.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.val = val;
  }

  @Override
  public byte[] key() {
    return key;
  }

  @Override
  public long dataLength() {
    return val.byteCount();
  }

  @Override
  public void write(OutputStream stream) throws IOException {
    val.write(stream);
    val.close();
  }

  @Override
  public int compareTo(DataChunkElement o) {
    return CmpUtil.compare(this.key(), o.key());
  }
}
