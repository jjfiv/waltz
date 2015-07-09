package edu.umass.cs.ciir.waltz.coders.map.impl;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class VocabEntryCoder<K> extends Coder<VocabEntry<K>> {
  private final Coder<K> keyCoder;
  private final Coder<Long> offsetCoder;

  public VocabEntryCoder(Coder<K> keyCoder) {
    this(keyCoder, FixedSize.longs);
  }

  public VocabEntryCoder(Coder<K> keyCoder, Coder<Long> offsetCoder) {
    this.offsetCoder = offsetCoder;
    this.keyCoder = keyCoder.lengthSafe();
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(VocabEntry<K> obj) throws IOException {
    BufferList output = new BufferList();
    output.add(keyCoder, obj.key);
    output.add(offsetCoder, obj.offset);
    return output;
  }

  @Nonnull
  @Override
  public VocabEntry<K> readImpl(InputStream inputStream) throws IOException {
    K key = keyCoder.readImpl(inputStream);
    long offset = offsetCoder.readImpl(inputStream);
    return new VocabEntry<>(key, offset);
  }
}
