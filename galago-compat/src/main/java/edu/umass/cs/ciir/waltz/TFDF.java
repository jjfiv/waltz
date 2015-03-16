package edu.umass.cs.ciir.waltz;

import edu.umass.cs.ciir.waltz.io.Coder;
import edu.umass.cs.ciir.waltz.io.coders.VByteCoders;
import edu.umass.cs.ciir.waltz.io.util.BufferList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author jfoley
 */
public class TFDF {
  public int tf;
  public int df;
  public TFDF() {
    this(0,0);
  }
  public TFDF(int tf, int df) {
    this.tf = tf;
    this.df = df;
  }
  public void add(TFDF other) {
    this.tf += other.tf;
    this.df += other.df;
  }

  public static Coder<TFDF> coder = new Coder<TFDF>() {
    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Override
    public ByteBuffer writeImpl(TFDF obj) throws IOException {
      BufferList bl = new BufferList();
      bl.add(VByteCoders.ints.write(obj.tf));
      bl.add(VByteCoders.ints.write(obj.df));
      return bl.asByteBuffer();
    }

    @Override
    public TFDF readImpl(InputStream inputStream) throws IOException {
      int tf = VByteCoders.ints.read(inputStream);
      int df = VByteCoders.ints.read(inputStream);
      return new TFDF(tf, df);
    }
  };
}
