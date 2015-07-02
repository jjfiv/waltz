package edu.umass.cs.ciir.waltz.flow.runtime;

import java.io.IOException;

/**
 * @author jfoley
 */
public interface FlowStateful {
  byte[] encode() throws IOException;
  void decode(byte[] data) throws IOException;
}
