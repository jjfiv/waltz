package edu.umass.cs.ciir.waltz.coders.map;

import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.map.rawsort.SortingRawIOMapWriter;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley
 */
public interface RawIOMapWriter extends Closeable {
  void put(DataChunk key, DataChunk val) throws IOException;

  /**
   * Returns a sorted version of this writer, so you don't have to sort the keys yourself :)
   * @return a sorter in front of this writer, or itself if the sorter is already present or not needed.
   * @throws IOException
   * @see SortingRawIOMapWriter
   */
  default RawIOMapWriter getSorting() throws IOException {
    return new SortingRawIOMapWriter(this);
  }
}
