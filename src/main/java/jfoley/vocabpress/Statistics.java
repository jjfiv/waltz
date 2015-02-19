package jfoley.vocabpress;

import ciir.jfoley.chai.errors.FatalError;
import org.lemurproject.galago.core.btree.simple.DiskMapWrapper;
import org.lemurproject.galago.utility.compression.VByte;

import java.io.*;

/**
* @author jfoley
*/
public class Statistics {
  public int tf;
  public int df;

  public Statistics() { this(0,0); }

  public Statistics(int tf, int df) {
    this.tf = tf;
    this.df = df;
  }

  public static DiskMapWrapper.Codec<Statistics> getCodec() {
    return StatsCodec.instance;
  }

  public static class StatsCodec implements DiskMapWrapper.Codec<Statistics> {
    public static StatsCodec instance = new StatsCodec();
    @Override
    public Statistics fromBytes(byte[] in) {
      try {
        ByteArrayInputStream bais = new ByteArrayInputStream(in);
        DataInputStream dis = new DataInputStream(bais);
        int tf = VByte.uncompressInt(dis);
        int df = 0;
        df = VByte.uncompressInt(dis);
        return new Statistics(tf, df);
      } catch (IOException e) {
        throw new FatalError(e);
      }
    }

    @Override
    public byte[] toBytes(Statistics stats) {
      try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        VByte.compressInt(dos, stats.tf);
        VByte.compressInt(dos, stats.df);
        return baos.toByteArray();
      } catch (IOException e) {
        throw new FatalError(e);
      }
    }

  }

}
