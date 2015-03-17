package edu.umass.cs.ciir.waltz.io.util;

import edu.umass.cs.ciir.waltz.io.Coder;

import java.nio.ByteBuffer;

/**
 * @author jfoley
 */
public interface MutableDataChunk extends DataChunk {
  /** write an object to this buffer list using the given codec. */
  public <T> void add(Coder<T> coder, T obj);
  public void add(ByteBuffer data);
  public void add(byte[] data);
  public void add(DataChunk data);
}
