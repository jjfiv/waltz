package jfoley.vocabpress;

import ciir.jfoley.chai.collections.util.ArrayFns;
import ciir.jfoley.chai.io.IO;
import org.lemurproject.galago.core.btree.simple.DiskMapReader;
import org.lemurproject.galago.core.btree.simple.DiskMapWrapper;
import org.lemurproject.galago.utility.ByteUtil;

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

  public static DiskMapWrapper.Codec<String> termCodec = new DiskMapWrapper.StringCodec();
  public static DiskMapWrapper.Codec<Statistics> statsCodec = Statistics.getCodec();
  public static DiskMapWrapper.Codec<Integer> intCodec = new VByteIntCodec();

  public Vocabulary(DiskMapReader dmr) {
    this.innerMap = dmr;
  }

  @Override
  public void close() throws IOException {
    IO.close(innerMap);
  }

  public String lookupTermString(int termId) throws IOException {
    byte[] data = innerMap.get(ArrayFns.concat(intCodec.toBytes(termId), IdSuffix));
    if(data == null) return null;
    return termCodec.fromBytes(data);
  }

  public Statistics lookupTermStats(int termId) throws IOException {
    byte[] data = innerMap.get(ArrayFns.concat(intCodec.toBytes(termId), StatsSuffix));
    if(data == null) return null;
    return statsCodec.fromBytes(data);
  }

  public int getTermId(String term) throws IOException {
    byte[] data = innerMap.get(ArrayFns.concat(termCodec.toBytes(term), TermSuffix));
    if(data == null) return -1;
    return intCodec.fromBytes(data);
  }

}
