package edu.umass.cs.ciir.waltz.coders.kinds.compress;

import ciir.jfoley.chai.io.StreamFns;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.CoderException;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * LZF is one of the fastest pure-java compression libraries.
 * This wraps any coder and gives it quick compression capabilities.
 * Note that this *does* length-prefix so its size is known.
 * @author jfoley
 */
public class GZipCoder<T> extends Coder<T> {
  final Coder<T> innerCoder;
  final Coder<Integer> sizeCoder = VarUInt.instance;

  public GZipCoder(Coder<T> innerCoder) {
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
    try (GZIPOutputStream lzfo = new GZIPOutputStream(baos)) {
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
      try (GZIPOutputStream lzfo = new GZIPOutputStream(baos)) {
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
        new GZIPInputStream(new ByteArrayInputStream(
            StreamFns.readBytes(inputStream, compressedSize))));
  }
}

