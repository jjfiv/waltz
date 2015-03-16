package edu.umass.cs.ciir.waltz.io.util;

import edu.umass.cs.ciir.waltz.io.Coder;

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

  /** write an object to this buffer list using the given codec. */
  public <T> void add(Coder<T> coder, T obj) {
    bufs.add(coder.write(obj));
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
    // Because put moves the position() of the buffer, we need to rewind before we return it.
    output.rewind();
    return output;
  }

  public int getByte(int index) {
    int start = 0;
    for (ByteBuffer buf : bufs) {
      if (index < start + buf.limit()) {
        return buf.get(index - start) & 0xff;
      }
      start += buf.limit();
    }
    return -1;
  }
}
