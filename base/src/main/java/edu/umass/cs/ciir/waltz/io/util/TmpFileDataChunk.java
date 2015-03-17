package edu.umass.cs.ciir.waltz.io.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * This data chunk immediately starts using a temporary file for storage of other DataChunks written to it.
 * @author jfoley
 */
public class TmpFileDataChunk implements DataChunk {
  private final File tmp;
  private final FileChannel channel;
  private long size;

  public TmpFileDataChunk() throws IOException {
    this.tmp = File.createTempFile("tfdc", "datachunk");
    FileOutputStream writer = new FileOutputStream(tmp.getAbsolutePath());
    this.channel = writer.getChannel();
    this.size = 0L;
  }
  @Override
  public int getByte(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long byteCount() {
    return size;
  }

  public void add(DataChunk chunk) throws IOException {
    size += chunk.byteCount();
    chunk.write(channel);
  }
  public void add(ByteBuffer buff) throws IOException {
    size += buff.limit();
    channel.write(buff);
  }

  @Override
  public ByteBuffer asByteBuffer() {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream asInputStream() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void write(OutputStream out) throws IOException {
    write(Channels.newChannel(out));
  }

  @Override
  public void write(WritableByteChannel out) throws IOException {
    channel.transferTo(0, size, out);
  }

  @Override
  public void close() throws IOException {
    channel.close();
    boolean status = tmp.delete();
    assert(status) : "Couldn't delete temporary file!";
  }
}
