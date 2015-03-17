package edu.umass.cs.ciir.waltz.io.streams;

import java.io.IOException;

/**
 * A reference to static data that can be turned into a SkipInputStream at any point, repeatedly.
 * @author jfoley
 */
public interface StaticStream {
  public SkipInputStream getNewStream() throws IOException;
}
