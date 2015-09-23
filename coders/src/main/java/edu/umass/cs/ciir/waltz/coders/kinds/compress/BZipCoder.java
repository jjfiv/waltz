package edu.umass.cs.ciir.waltz.coders.kinds.compress;

import ciir.jfoley.chai.io.StreamFns;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.CoderException;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import javax.annotation.Nonnull;
import java.io.*;

/**
 * @author jfoley
 */
public class BZipCoder<T> extends Coder<T> {
  final Coder<T> innerCoder;
  final Coder<Integer> sizeCoder = VarUInt.instance;

  public BZipCoder(Coder<T> innerCoder) {
    this.innerCoder = innerCoder;
  }

  @Nonnull
  @Override
  public Class<?> getTargetClass() {
    return innerCoder.getTargetClass();
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
    try (BZip2CompressorOutputStream lzfo = new BZip2CompressorOutputStream(baos)) {
      innerCoder.write(lzfo, obj);
    }

    // length-prefix:
    ByteBuilder output = new ByteBuilder();
    byte[] compressed = baos.toByteArray();
    output.add(sizeCoder, compressed.length);
    output.add(compressed);
    return output;
  }

  public void write(OutputStream out, T elem) {
    try {
      // compress:
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (BZip2CompressorOutputStream lzfo = new BZip2CompressorOutputStream(baos)) {
        innerCoder.write(lzfo, elem);
      }

      // length-prefix:
      sizeCoder.write(out, baos.size());
      baos.writeTo(out);
    } catch (IOException ioe) {
      throw new CoderException(ioe, this.getClass());
    }
  }

  @Nonnull
  @Override
  public T readImpl(InputStream inputStream) throws IOException {
    int compressedSize = sizeCoder.readImpl(inputStream);
    return innerCoder.read(
        new BZip2CompressorInputStream(new ByteArrayInputStream(
            StreamFns.readBytes(inputStream, compressedSize))));
  }
}
