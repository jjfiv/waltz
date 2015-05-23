package edu.umass.cs.ciir.waltz.coders.files;

import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author jfoley.
 */
public abstract class DataSource implements Closeable {
  public abstract long size() throws IOException;
  public abstract ByteBuffer read(long position, int size) throws IOException;
  public abstract DataSource view(long position, long size) throws IOException;

  /**
   * Read a long at the given position in this source.
   * @param position the offset to read from.
   * @return the long value.
   * @throws IOException on any sort of problems.
   */
  public long readLong(long position) throws IOException {
    return FixedSize.longs.read(read(position, 8));
  }

}
