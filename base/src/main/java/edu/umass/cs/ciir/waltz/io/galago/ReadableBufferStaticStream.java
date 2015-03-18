package edu.umass.cs.ciir.waltz.io.galago;

import edu.umass.cs.ciir.waltz.io.streams.SkipInputStream;
import edu.umass.cs.ciir.waltz.io.streams.StaticStream;
import org.lemurproject.galago.utility.btree.disk.DiskBTreeIterator;
import org.lemurproject.galago.utility.buffer.CachedBufferDataStream;
import org.lemurproject.galago.utility.buffer.ReadableBuffer;

import java.io.IOException;

/**
* @author jfoley
*/
class ReadableBufferStaticStream implements StaticStream {
  private final long start;
  private final long end;
  private final ReadableBuffer file;

  public ReadableBufferStaticStream(DiskBTreeIterator iterator) throws IOException {
    this.start = iterator.getValueStart();
    this.end = iterator.getValueEnd();
    this.file = iterator.input;
  }

  @Override
  public SkipInputStream getNewStream() {
    return new GalagoSkipInputStream(new CachedBufferDataStream(file, start, end));
  }

  @Override
  public long length() {
    return end - start;
  }
}
