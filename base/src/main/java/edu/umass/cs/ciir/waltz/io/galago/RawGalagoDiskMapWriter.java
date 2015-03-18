package edu.umass.cs.ciir.waltz.io.galago;

import edu.umass.cs.ciir.waltz.io.map.RawIOMapWriter;
import edu.umass.cs.ciir.waltz.io.util.DataChunk;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.btree.disk.DiskBTreeWriter;

import java.io.IOException;

/**
 * @author jfoley
 */
public class RawGalagoDiskMapWriter implements RawIOMapWriter {
  private final DiskBTreeWriter writer;

  public RawGalagoDiskMapWriter(DiskBTreeWriter writer) {
    this.writer = writer;
  }
  public RawGalagoDiskMapWriter(String path, Parameters argp) throws IOException {
    this(new DiskBTreeWriter(path, argp));
  }
  public RawGalagoDiskMapWriter(String path) throws IOException {
    this(new DiskBTreeWriter(path));
  }

  @Override
  public void put(DataChunk key, DataChunk val) throws IOException {
    writer.add(new DataChunkElement(key, val));
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}
