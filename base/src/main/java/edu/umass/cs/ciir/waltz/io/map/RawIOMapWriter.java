package edu.umass.cs.ciir.waltz.io.map;

import edu.umass.cs.ciir.waltz.io.util.DataChunk;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley
 */
public interface RawIOMapWriter extends Closeable {
  public void put(DataChunk key, DataChunk val) throws IOException;
}
