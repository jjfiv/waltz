package edu.umass.cs.ciir.waltz.io.postings.format;

import edu.umass.cs.ciir.waltz.coders.ints.IntsCoder;
import edu.umass.cs.ciir.waltz.coders.kinds.DeltaIntListCoder;

/**
 * @author jfoley
 */
public class BlockedPostingsFormat {
  public static int DEFAULT_BLOCKSIZE = 128;
  public static IntsCoder DEFAULT_INTSCODER = new DeltaIntListCoder();
}
