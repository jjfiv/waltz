package edu.umass.cs.ciir.waltz.coders.files;

import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;

import java.io.IOException;

/**
 * @author jfoley
 */
public class WriteLongLater {
  public final DataSink file;
  public long offset;

  public WriteLongLater(DataSink file) throws IOException {
    this.file = file;
    this.offset = file.tell();
    file.write(FixedSize.longs, 0xdeadbeefdeadbeefL);
  }

  public void write(long value) throws IOException {
    file.write(offset, FixedSize.longs, value);
  }
}
