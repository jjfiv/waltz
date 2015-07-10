package edu.umass.cs.ciir.waltz.io.postings.streaming;

import ciir.jfoley.chai.IntMath;
import ciir.jfoley.chai.io.StreamFns;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteArray;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
class ByteKeyPostingCoder<V> extends Coder<ByteKeyPosting<V>> {
  private final Coder<V> valCoder;

  public ByteKeyPostingCoder(Coder<V> valCoder) {
    this.valCoder = valCoder.lengthSafe();
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(ByteKeyPosting<V> obj) throws IOException {
    ByteBuilder output = new ByteBuilder();
    output.add(VarUInt.instance, IntMath.fromLong(obj.key.byteCount()));
    output.add(obj.key);
    output.add(VarUInt.instance, obj.document);
    output.add(valCoder, obj.value);
    return output;
  }

  @Nonnull
  @Override
  public ByteKeyPosting<V> readImpl(InputStream inputStream) throws IOException {
    int size = VarUInt.instance.readImpl(inputStream);
    ByteArray key = new ByteArray(StreamFns.readBytes(inputStream, size));
    int document = VarUInt.instance.readImpl(inputStream);
    V value = valCoder.readImpl(inputStream);
    return new ByteKeyPosting<>(key, document, value);
  }
}
