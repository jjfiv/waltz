package edu.umass.cs.ciir.waltz.galago.io;

import edu.umass.cs.ciir.waltz.coders.data.ByteArray;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.map.RawIOMapWriter;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.btree.disk.DiskBTreeWriter;

import java.io.IOException;

/**
 * @author jfoley
 */
public class RawGalagoDiskMapWriter implements RawIOMapWriter {
  private final DiskBTreeWriter writer;
  private ByteArray lastKey;

  public RawGalagoDiskMapWriter(DiskBTreeWriter writer) {
    this.writer = writer;
    this.lastKey = null;
  }
  public RawGalagoDiskMapWriter(String path, Parameters argp) throws IOException {
    this(new DiskBTreeWriter(path, argp));
  }
  public RawGalagoDiskMapWriter(String path) throws IOException {
    this(new DiskBTreeWriter(path));
  }

  @Override
  public void put(DataChunk key, DataChunk val) throws IOException {
    if(lastKey != null && lastKey.equals(ByteArray.of(key))) {
      throw new IllegalArgumentException("We don't support multiple of the same key in an RawGalagoDiskMap: "+lastKey+" "+key);
    }
    writer.add(new DataChunkElement(key, val));
    this.lastKey = ByteArray.of(key);
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}
