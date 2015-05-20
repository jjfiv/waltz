package edu.umass.cs.ciir.waltz.coders.streams;

/**
 * A reference to static data that can be turned into a SkipInputStream at any point, repeatedly.
 * @author jfoley
 */
public interface StaticStream {
  SkipInputStream getNewStream();
  long length();
}
