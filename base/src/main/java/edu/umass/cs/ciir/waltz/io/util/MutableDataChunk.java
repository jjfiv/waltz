package edu.umass.cs.ciir.waltz.io.util;

import edu.umass.cs.ciir.waltz.io.Coder;

import java.nio.ByteBuffer;

/**
 * @author jfoley
 */
public interface MutableDataChunk extends DataChunk {
  /** write an object to this buffer list using the given codec. */
  <T> void add(Coder<T> coder, T obj);
  void add(ByteBuffer data);
  void add(byte[] data);
  void add(DataChunk data);
}
