package edu.umass.cs.ciir.waltz.sys;

import ciir.jfoley.chai.io.Directory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.ints.IntsCoder;
import edu.umass.cs.ciir.waltz.coders.kinds.DeltaIntListCoder;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapReader;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapWriter;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.sys.positions.AccumulatingPositionsWriter;
import edu.umass.cs.ciir.waltz.sys.positions.PIndexWriter;
import edu.umass.cs.ciir.waltz.sys.tmp.TmpStreamPostingIndexWriter;

import java.io.IOException;
import java.util.Comparator;

/**
 * @author jfoley
 */
public final class PostingsConfig<K, V> {
  public final Coder<K> keyCoder;
  public final Coder<V> valCoder;
  public final Comparator<K> keyCmp;
  public final KeyMetadata<V> metadata;

  // make slightly more efficient:
  public IntsCoder docsCoder = new DeltaIntListCoder(VarUInt.instance, VarUInt.instance);
  public int blockSize = 128;

  public PostingsConfig(Coder<K> keyCoder, Coder<V> valCoder, Comparator<K> keyCmp, KeyMetadata<V> metadata) {
    this.keyCoder = keyCoder.lengthSafe();
    this.valCoder = valCoder.lengthSafe();
    this.keyCmp = keyCmp;
    this.metadata = metadata;
  }

  public KeyMetadata<V> newMetadata() {
    return metadata.zero();
  }

  public TmpStreamPostingIndexWriter<K, V> makeTemporaryWriter(Directory outdir, String baseName) {
    return new TmpStreamPostingIndexWriter<>(outdir, baseName, this);
  }

  public BlockedPostingsWriter<K, V> makeFinalWriter(Directory outdir, String baseName) throws IOException {
    return new BlockedPostingsWriter<>(this,
        new WaltzDiskMapWriter<>(
            outdir,
            baseName,
            this.keyCoder,
            null
        ));
  }


  public WaltzDiskMapReader<K, PostingMover<V>> openReader(Directory input, String positions) throws IOException {
    return new WaltzDiskMapReader<>(input, positions, this.keyCoder, new ReadOnlyPostingsCoder<>(this));
  }

  public PIndexWriter<K,V> getWriter(Directory input, String baseName) throws IOException {
    return new PIndexWriter<>(this, input, baseName);
  }

  /**
   * Return a helper wrapper which accumulates [word,doc,pos] tuples and flushes when doc changes.
   * @param input the directory containing all index parts.
   * @param baseName the base name of this index part (may make multiple files [.keys, .values etc])
   * @return A closeable writer: {@link AccumulatingPositionsWriter}
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public AccumulatingPositionsWriter<K> getPositionsWriter(Directory input, String baseName) throws IOException {
    assert(PositionsList.class.isAssignableFrom(this.valCoder.getTargetClass()));
    PostingsConfig<K,PositionsList> self = (PostingsConfig<K,PositionsList>) this;
    return new AccumulatingPositionsWriter<>(new PIndexWriter<>(self, input, baseName));
  }

  @SuppressWarnings("unchecked")
  public Class<K> getKeyClass() {
    return (Class<K>) keyCoder.getTargetClass();
  }
}
