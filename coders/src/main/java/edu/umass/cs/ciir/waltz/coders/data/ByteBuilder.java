package edu.umass.cs.ciir.waltz.coders.data;

import edu.umass.cs.ciir.waltz.coders.Coder;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * @author jfoley
 */
public class ByteBuilder implements MutableDataChunk {
  final ByteArrayOutputStream baos;
  final WritableByteChannel asChannel;

  public ByteBuilder() {
    this.baos = new ByteArrayOutputStream();
    this.asChannel = Channels.newChannel(baos);
  }

  @Override
  public <T> void add(Coder<T> coder, T obj) {
    coder.write(baos, obj);
  }

  @Override
  public void add(ByteBuffer data) {
    try {
      asChannel.write(data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void add(byte[] data) {
    try {
      baos.write(data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void add(DataChunk data) {
    try {
      data.write(baos);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int getByte(int index) {
    return baos.toByteArray()[index];
  }

  @Override
  public long byteCount() {
    return baos.size();
  }

  @Override
  public ByteBuffer asByteBuffer() {
    return ByteBuffer.wrap(baos.toByteArray());
  }

  @Override
  public byte[] asByteArray() {
    return baos.toByteArray();
  }

  @Override
  public InputStream asInputStream() {
    return new ByteArrayInputStream(baos.toByteArray());
  }

  @Override
  public void write(OutputStream out) throws IOException {
    baos.writeTo(out);
  }

  @Override
  public void write(WritableByteChannel out) throws IOException {
    baos.writeTo(Channels.newOutputStream(out));
  }

  @Override
  public void close() throws IOException {
    baos.reset();
    baos.close();
  }

  public boolean isEmpty() {
    return byteCount() == 0;
  }

  public void clear() {
    baos.reset();
  }
}
