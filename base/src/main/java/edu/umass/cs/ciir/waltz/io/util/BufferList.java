package edu.umass.cs.ciir.waltz.io.util;

import edu.umass.cs.ciir.waltz.io.Coder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author jfoley
 */
public class BufferList implements DataChunk {
  List<DataChunk> bufs;

  public BufferList() {
    this.bufs = new ArrayList<>();
  }
  public BufferList(Collection<? extends DataChunk> data) {
    this.bufs = new ArrayList<>(data);
  }

  /** Returns the total size in bytes. */
  @Override
  public int byteCount() {
    int size = 0;
    for (DataChunk buf : bufs) {
      size += buf.byteCount();
    }
    return size;
  }

  /** write an object to this buffer list using the given codec. */
  public <T> void add(Coder<T> coder, T obj) {
    bufs.add(coder.writeData(obj));
  }
  public void add(ByteBuffer data) {
    bufs.add(ByteBufferDataChunk.of(data));
  }
  public void add(byte[] data) {
    add(ByteBuffer.wrap(data));
  }
  public void add(DataChunk data) {
    if(data instanceof BufferList) {
      bufs.addAll(((BufferList) data).bufs);
    } else {
      bufs.add(data);
    }
  }

  @Override
  public ByteBuffer asByteBuffer() {
    ByteBuffer output = ByteBuffer.allocate(byteCount());
    for (DataChunk buf : bufs) {
      output.put(buf.asByteBuffer());
    }
    // Because put moves the position() of the buffer, we need to rewind before we return it.
    output.rewind();
    return output;
  }

  @Override
  public InputStream asInputStream() {
    // Kind of a hack; might be more performant to be lazy about it.
    return new ByteArrayInputStream(asByteBuffer().array());
  }

  @Override
  public int getByte(int index) {
    int start = 0;
    for (DataChunk buf : bufs) {
      if (index < start + buf.byteCount()) {
        return buf.getByte(index - start);
      }
      start += buf.byteCount();
    }
    return -1;
  }
}
