package edu.umass.cs.ciir.waltz.postings.docset;

import ciir.jfoley.chai.io.Directory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.map.WrappedIOMap;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapReader;
import edu.umass.cs.ciir.waltz.dociter.movement.Mover;

import java.io.IOException;

/**
 * @author jfoley
 */
public class DocumentSetReader<K> extends WrappedIOMap<K,Mover> {
  public DocumentSetReader(Coder<K> keyCoder, Directory baseDir, String baseName) throws IOException {
    super(new WaltzDiskMapReader<>(
        baseDir,
        baseName,
        keyCoder,
        new DeltaIntListMoverCoder(FixedSize.ints, VarUInt.instance)
    ));
  }
}
