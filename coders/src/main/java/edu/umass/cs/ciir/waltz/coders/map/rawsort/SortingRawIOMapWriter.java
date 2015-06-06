package edu.umass.cs.ciir.waltz.coders.map.rawsort;

import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.map.RawIOMapWriter;
import edu.umass.cs.ciir.waltz.coders.sorter.ExternalSortingWriter;

import java.io.IOException;

/**
 * @author jfoley.
 */
public class SortingRawIOMapWriter implements RawIOMapWriter {
  private final TemporaryDirectory tmpdir;
  private final ExternalSortingWriter<DataChunkPair> sorter;
  private final RawIOMapWriter writer;

  public SortingRawIOMapWriter(RawIOMapWriter writer) throws IOException {
    this.tmpdir = new TemporaryDirectory();
    this.sorter = new ExternalSortingWriter<>(tmpdir.get(), new DataChunkPairCoder());
    this.writer = writer;
  }

  @Override
  public void put(DataChunk key, DataChunk val) throws IOException {
    sorter.process(new DataChunkPair(key, val));
  }

  @Override
  public void close() throws IOException {
    sorter.close();
    for (DataChunkPair dataChunkPair : sorter.getOutput()) {
      writer.put(dataChunkPair.getKey(), dataChunkPair.getValue());
    }
    writer.close();
    tmpdir.close();
  }

  @Override
  public RawIOMapWriter getSorting() {
    return this;
  }

}
