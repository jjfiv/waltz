package edu.umass.cs.ciir.waltz.galago.io;

import ciir.jfoley.chai.lang.annotations.EmergencyUseOnly;
import edu.umass.cs.ciir.waltz.coders.streams.SkipInputStream;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;
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

  // Convenience method, probably shouldn't be here...
  @EmergencyUseOnly
  public ReadableBufferStaticStream(DiskBTreeIterator iterator) throws IOException {
    this(iterator.input, iterator.getValueStart(), iterator.getValueEnd());
  }
  public ReadableBufferStaticStream(ReadableBuffer file, long start, long end) throws IOException {
    this.start = start;
    this.end = end;
    this.file = file;
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
