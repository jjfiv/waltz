package edu.umass.cs.ciir.waltz.io.postings.format;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.DeltaIntListCoder;

import java.util.List;

/**
 * @author jfoley
 */
public class SimplePostingListFormat {
  public static int DEFAULT_BLOCKSIZE = 128;
  public static Coder<List<Integer>> DEFAULT_INTSCODER = new DeltaIntListCoder();

}
