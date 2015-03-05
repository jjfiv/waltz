package jfoley.vocabpress.io;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author jfoley
 */
public class BufferList {
  List<ByteBuffer> bufs;

  public BufferList() {
    this.bufs = new ArrayList<>();
  }
  public BufferList(Collection<ByteBuffer> data) {
    this.bufs = new ArrayList<>(data);
  }

  /** Returns the total size in bytes. */
  public int byteCount() {
    int size = 0;
    for (ByteBuffer buf : bufs) {
      size += buf.limit();
    }
    return size;
  }

  public void add(ByteBuffer data) {
    bufs.add(data);
  }
  public void add(byte[] data) {
    add(ByteBuffer.wrap(data));
  }

  public ByteBuffer compact() {
    ByteBuffer output = ByteBuffer.allocate(byteCount());
    for (ByteBuffer buf : bufs) {
      output.put(buf);
    }
    return output;
  }

}
