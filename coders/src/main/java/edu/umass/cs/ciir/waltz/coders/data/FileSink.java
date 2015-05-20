package edu.umass.cs.ciir.waltz.coders.data;

import edu.umass.cs.ciir.waltz.coders.Coder;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static java.nio.file.StandardOpenOption.*;

/**
 * @author jfoley
 */
public class FileSink implements Closeable {
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

  @Override
  public void close() throws IOException {
    channel.close();
  }

  public <T> void write(Coder<T> coder, T termStats) throws IOException {
    channel.write(coder.write(termStats));
  }
}
