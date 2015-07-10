package edu.umass.cs.ciir.waltz.postings.docset;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.DeltaIntListCoder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author jfoley
 */
public class DocumentSetChunkCoder<K> extends Coder<DocumentSetChunk<K>> {
  public final Coder<K> keyCoder;
  public final Coder<List<Integer>> listCoder;

  public DocumentSetChunkCoder(Coder<K> keyCoder) {
    this(keyCoder, new DeltaIntListCoder());
  }

  public DocumentSetChunkCoder(Coder<K> keyCoder, Coder<List<Integer>> listCoder) {
    this.keyCoder = keyCoder.lengthSafe();
    this.listCoder = listCoder.lengthSafe();
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(DocumentSetChunk<K> obj) throws IOException {
    ByteBuilder output = new ByteBuilder();
    output.add(keyCoder, obj.key);
    output.add(listCoder, obj.docs);
    return output;
  }

  @Nonnull
  @Override
  public DocumentSetChunk<K> readImpl(InputStream inputStream) throws IOException {
    K key = keyCoder.readImpl(inputStream);
    IntList docs = new IntList();
    docs.addAll(listCoder.readImpl(inputStream));
    return new DocumentSetChunk<>(key, docs);
  }
}
