package edu.umass.cs.ciir.waltz.io.postings;

import edu.umass.cs.ciir.waltz.io.Coder;

/**
 * @author jfoley
 */
public interface Statistics<Posting> {
  public void add(Posting p);
  public Coder<Statistics<Posting>> getCoder();
}
