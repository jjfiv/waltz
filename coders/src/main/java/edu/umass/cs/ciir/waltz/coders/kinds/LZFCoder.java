package edu.umass.cs.ciir.waltz.coders.kinds;

import ciir.jfoley.chai.io.StreamFns;
import com.ning.compress.lzf.LZFDecoder;
import com.ning.compress.lzf.LZFOutputStream;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * LZF is one of the fastest pure-java compression libraries.
 * This wraps any coder and gives it quick compression capabilities.
 * Note that this *does* length-prefix so its size is known.
 * @author jfoley
 */
public class LZFCoder<T> extends Coder<T> {
  final Coder<T> innerCoder;
  final Coder<Integer> sizeCoder = VarUInt.instance;

  public LZFCoder(Coder<T> innerCoder) {
    this.innerCoder = innerCoder;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(T obj) throws IOException {
    // compress:
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (LZFOutputStream lzfo = new LZFOutputStream(baos)) {
      innerCoder.write(lzfo, obj);
    }

    // length-prefix:
    BufferList output = new BufferList();
    byte[] compressed = baos.toByteArray();
    output.add(sizeCoder, compressed.length);
    output.add(compressed);
    return output;
  }

  @Nonnull
  @Override
  public T readImpl(InputStream inputStream) throws IOException {
    int compressedSize = sizeCoder.readImpl(inputStream);
    return innerCoder.read(
        LZFDecoder.decode(
            StreamFns.readBytes(inputStream, compressedSize)));
  }
}
