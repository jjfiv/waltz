package edu.umass.cs.ciir.waltz.sys;

import ciir.jfoley.chai.io.Directory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.ints.IntsCoder;
import edu.umass.cs.ciir.waltz.coders.kinds.DeltaIntListCoder;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapReader;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapWriter;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;

import java.io.IOException;
import java.util.Comparator;

/**
 * @author jfoley
 */
public final class PostingsConfig<K, M extends KeyMetadata<V, M>, V> {
  public final Coder<K> keyCoder;
  public final Coder<M> metadataCoder;
  public final Coder<V> valCoder;
  public final Comparator<K> keyCmp;
  private final KeyMetadata<V, M> metadata;

  // make slightly more efficient:
  public IntsCoder docsCoder = new DeltaIntListCoder(VarUInt.instance, VarUInt.instance);
  public int blockSize = 128;

  public PostingsConfig(Coder<K> keyCoder, Coder<M> metadataCoder, Coder<V> valCoder, Comparator<K> keyCmp, M metadata) {
    this.keyCoder = keyCoder.lengthSafe();
    this.metadataCoder = metadataCoder.lengthSafe();
    this.valCoder = valCoder.lengthSafe();
    this.keyCmp = keyCmp;
    this.metadata = metadata;
  }

  public M newMetadata() {
    return metadata.zero();
  }

  public TmpStreamPostingIndexWriter<K, M, V> makeTemporaryWriter(Directory outdir, String baseName) {
    return new TmpStreamPostingIndexWriter<>(outdir, baseName, this);
  }

  public BlockedPostingsWriter<K, M, V> makeFinalWriter(Directory outdir, String baseName) throws IOException {
    return new BlockedPostingsWriter<>(this,
        new WaltzDiskMapWriter<>(
            outdir,
            baseName,
            this.keyCoder,
            null,
            false
        ));
  }


  public WaltzDiskMapReader<K, PostingMover<V>> openReader(Directory input, String positions) throws IOException {
    return new WaltzDiskMapReader<>(input, positions, this.keyCoder, new ReadOnlyPostingsCoder<>(this));
  }

  public PositionsIndexFile.PIndexWriter<K> getWriter(Directory input, String baseName) throws IOException {
    return new PositionsIndexFile.PIndexWriter<>(this.keyCoder, input, baseName);
  }
}
