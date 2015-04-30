package edu.umass.cs.ciir.waltz.io.map;

import ciir.jfoley.chai.collections.Pair;
import edu.umass.cs.ciir.waltz.io.streams.StaticStream;
import edu.umass.cs.ciir.waltz.io.util.DataChunk;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley
 */
public interface RawIOMap extends Closeable {
  long keyCount();
  Map<String, Object> getConfig();
  StaticStream get(DataChunk key) throws IOException;
  List<Pair<DataChunk, StaticStream>> getInBulk(List<DataChunk> keys) throws IOException;
}
