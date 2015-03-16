package edu.umass.cs.ciir.waltz;

import ciir.jfoley.chai.errors.FatalError;
import org.lemurproject.galago.core.btree.simple.DiskMapWrapper;
import org.lemurproject.galago.utility.compression.VByte;

import java.io.*;

/**
* @author jfoley
*/
public class StatsCodec implements DiskMapWrapper.Codec<Statistics> {
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
