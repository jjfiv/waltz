package edu.umass.cs.ciir.waltz.io.map;

import edu.umass.cs.ciir.waltz.io.util.DataChunk;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley
 */
public interface RawIOMapWriter extends Closeable {
  void put(DataChunk key, DataChunk val) throws IOException;
}
