package edu.umass.cs.ciir.waltz;

import ciir.jfoley.chai.errors.FatalError;
import org.lemurproject.galago.core.btree.simple.DiskMapWrapper;
import org.lemurproject.galago.utility.compression.VByte;

import java.io.IOException;

/**
* @author jfoley
*/
public class VByteIntCodec implements DiskMapWrapper.Codec<Integer> {

  @Override
  public Integer fromBytes(byte[] in) {
    try {
      return VByte.uncompressInt(in, 0);
    } catch (IOException e) {
      throw new FatalError(e);
    }
  }

  @Override
  public byte[] toBytes(Integer out) {
    try {
      return VByte.compressInt(out);
    } catch (IOException e) {
      throw new FatalError(e);
    }
  }
}
