package edu.umass.cs.ciir.waltz.coders.sorter;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.files.FileSink;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPOutputStream;

/**
 * Writes a stream of objects to a file and prefixes with a count.
 * @author jfoley.
 */
public class RunWriter<T> implements ClosingSinkFn<T> {
  public static final Coder<Long> countCoder = FixedSize.longs;
  private final File outputFile;
  private final Coder<T> itemCoder;
  private final OutputStream outputStream;
  private long count;

  public RunWriter(Coder<T> itemCoder, File outputFile) throws IOException {
    FileSink output = new FileSink(outputFile); // don't need to close this as the stream takes over.
    output.write(countCoder, 0L); // temporarily write a zero.
    this.count = 0;
    this.outputFile = outputFile;
    this.outputStream = new GZIPOutputStream(output.getOutputStream());
    this.itemCoder = itemCoder;
  }

  @Override
  public void close() throws IOException {
    // close stream output.
    outputStream.close();
    // patch up count at the beginning again:
    try (FileChannel output = FileChannel.open(outputFile.toPath(), StandardOpenOption.WRITE)) {
      output.write(countCoder.write(count), 0);
    }
  }

  @Override
  public void process(T input) {
    count++;
    itemCoder.write(outputStream, input);
  }
}
