package edu.umass.cs.ciir.waltz.coders.files;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

import static java.nio.file.StandardOpenOption.*;

/**
 * @author jfoley
 */
public class FileSink implements Closeable, Flushable {
  private final FileChannel channel;

  public FileSink(String outputPath) throws IOException {
    this(new File(outputPath));
  }

  public FileSink(File file) throws IOException {
    this.channel = FileChannel.open(file.toPath(), CREATE, WRITE, READ);
  }

  public void write(ByteBuffer buf) throws IOException {
    channel.write(buf);
  }

  public void write(DataChunk data) throws IOException {
    data.write(channel);
  }

  public long tell() throws IOException {
    return channel.position();
  }

  @Override
  public void close() throws IOException {
    channel.close();
  }

  public <T> void write(Coder<T> coder, T obj) throws IOException {
    channel.write(coder.write(obj));
  }

  public OutputStream getOutputStream() {
    return Channels.newOutputStream(channel);
  }

  @Override
  public void flush() throws IOException {
    channel.force(false); // don't force changes to metadata
  }

  public <T> void write(long offset, Coder<T> coder, T value) throws IOException {
    channel.write(coder.write(value), offset);
  }
}
