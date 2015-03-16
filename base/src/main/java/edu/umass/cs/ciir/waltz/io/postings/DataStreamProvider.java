package edu.umass.cs.ciir.waltz.io.postings;

import java.io.InputStream;

/**
 * Interface to Input -> something that provides a bounded InputStream (DataStream).
 *
 * DataStream adds "seek(relative to stream start point)" to the otherwise normal InputStream/DataInput interfaces.
 * @author jfoley
 */
public interface DataStreamProvider {
  public InputStream getInputStream();
}
