package edu.umass.cs.ciir.waltz.sys.positions;

import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.io.Directory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.map.IOMap;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.io.postings.PositionsListCoder;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.sys.PostingsConfig;

import java.io.IOException;

/**
 * @author jfoley
 */
public class PositionsIndexFile {

  public static <K> IOMap<K, PostingMover<PositionsList>> openReader(Coder<K> keyCoder, Directory input) throws IOException {
    return openReader(keyCoder, input, "positions");
  }
  public static <K> IOMap<K, PostingMover<PositionsList>> openReader(Coder<K> keyCoder, Directory input, String baseName) throws IOException {
    PostingsConfig<K,PositionsList> cfg = new PostingsConfig<>(
        keyCoder,
        new PositionsListCoder(),
        Comparing.defaultComparator(),
        new PositionsCountMetadata()
    );

    return cfg.openReader(input, baseName);
  }
}
