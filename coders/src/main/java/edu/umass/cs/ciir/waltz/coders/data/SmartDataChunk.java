package edu.umass.cs.ciir.waltz.coders.data;

import ciir.jfoley.chai.IntMath;
import ciir.jfoley.chai.collections.util.ArrayFns;
import ciir.jfoley.chai.io.IO;
import ciir.jfoley.chai.jvm.MemoryNotifier;
import ciir.jfoley.chai.lang.annotations.EmergencyUseOnly;
import edu.umass.cs.ciir.waltz.coders.Coder;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * The SmartDataChunk is either backed by a {@link BufferList} or a {@link TmpFileDataChunk}
 * If the size exceeds a given amount, it will move out of memory and onto disk.
 * Also, if you call flush(), or if the {@link MemoryNotifier} does, it forces the data to disk.
 * @see BufferList
 * @see TmpFileDataChunk
 * @see MemoryNotifier
 * @author jfoley
 */
public class SmartDataChunk implements MutableDataChunk, Flushable {
  private BufferList bufferList;
  private TmpFileDataChunk tmpFile;

  public SmartDataChunk() {
    MemoryNotifier.register(this);
    bufferList = new BufferList();
    tmpFile = null;
  }

  @Override
  public synchronized <T> void add(Coder<T> coder, T obj) {
    bufferList.add(coder, obj);
  }

  @Override
  public synchronized void add(ByteBuffer data) {
    bufferList.add(data);
  }

  @Override
  public synchronized void add(byte[] data) {
    bufferList.add(data);
  }

  @Override
  public synchronized void add(DataChunk data) {
    bufferList.add(data);
  }

  @Override
  public synchronized int getByte(int index) {
    if(tmpFile != null) {
      if(index < tmpFile.byteCount()) {
        return tmpFile.getByte(index);
      } else {
        return bufferList.getByte(IntMath.fromLong(index - tmpFile.byteCount()));
      }
    }
    return bufferList.getByte(index);
  }

  @Override
  public synchronized long byteCount() {
    long tfbc = (tmpFile == null) ? 0 : tmpFile.byteCount();
    return tfbc + bufferList.byteCount();
  }

  @EmergencyUseOnly
  @Override
  public ByteBuffer asByteBuffer() {
    return ByteBuffer.wrap(asByteArray());
  }

  @EmergencyUseOnly
  @Override
  public synchronized byte[] asByteArray() {
    if(tmpFile != null) {
      return ArrayFns.concat(tmpFile.asByteArray(), bufferList.asByteArray());
    } else {
      return bufferList.asByteArray();
    }
  }

  @Override
  public synchronized InputStream asInputStream() {
    if(tmpFile!=null) {
      return new SequenceInputStream(tmpFile.asInputStream(), bufferList.asInputStream());
    } else {
      return bufferList.asInputStream();
    }
  }

  @Override
  public synchronized void write(OutputStream out) throws IOException {
    if(tmpFile != null) {
      tmpFile.write(out);
    }
    bufferList.write(out);
  }

  @Override
  public synchronized void write(WritableByteChannel out) throws IOException {
    if(tmpFile != null) {
      tmpFile.write(out);
    }
    bufferList.write(out);
  }

  @Override
  public void close() throws IOException {
    MemoryNotifier.unregister(this);
    bufferList.close();
    IO.close(tmpFile);
  }

  @Override
  public synchronized void flush() throws IOException {
    if(tmpFile == null) {
      tmpFile = new TmpFileDataChunk();
    }
    tmpFile.add(bufferList);
    bufferList.clear();
  }
}
