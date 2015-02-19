package jfoley.vocabpress;

import ciir.jfoley.chai.collections.util.ArrayFns;
import ciir.jfoley.chai.io.IO;
import org.lemurproject.galago.core.btree.simple.DiskMapReader;
import org.lemurproject.galago.utility.ByteUtil;
import org.lemurproject.galago.utility.compression.VByte;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * @author jfoley
 */
public class Vocabulary implements Closeable {
  Map<byte[],byte[]> innerMap;

  public static byte[] IdSuffix = ByteUtil.fromString("i");
  public static byte[] TermSuffix = ByteUtil.fromString("t");
  public static byte[] StatsSuffix = ByteUtil.fromString("s");

  public Vocabulary(DiskMapReader dmr) {
    this.innerMap = dmr;
  }

  @Override
  public void close() throws IOException {
    IO.close(innerMap);
  }

  public String lookupTermString(int termId) throws IOException {
    byte[] data = innerMap.get(ArrayFns.concat(VByte.compressInt(termId), IdSuffix));
    if(data == null) {
      return null;
    }
    return ByteUtil.toString(data);
  }

  public Statistics lookupTermStats(int termId) throws IOException {
    byte[] data = innerMap.get(ArrayFns.concat(VByte.compressInt(termId), StatsSuffix));
    if(data == null) return null;
    return Statistics.getCodec().fromBytes(data);
  }

}
