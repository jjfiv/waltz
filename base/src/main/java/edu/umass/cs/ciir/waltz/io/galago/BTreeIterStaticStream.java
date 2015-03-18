package edu.umass.cs.ciir.waltz.io.galago;

import edu.umass.cs.ciir.waltz.io.streams.SkipInputStream;
import edu.umass.cs.ciir.waltz.io.streams.StaticStream;
import org.lemurproject.galago.utility.btree.disk.DiskBTreeIterator;

import java.io.IOException;

/**
* @author jfoley
*/
class BTreeIterStaticStream implements StaticStream {
  private final DiskBTreeIterator iterator;

  public BTreeIterStaticStream(DiskBTreeIterator iterator) {
    this.iterator = iterator;
  }

  @Override
  public SkipInputStream getNewStream() {
    try {
      return new GalagoSkipInputStream(iterator.getValueStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long length() {
    try {
      return iterator.getValueLength();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
