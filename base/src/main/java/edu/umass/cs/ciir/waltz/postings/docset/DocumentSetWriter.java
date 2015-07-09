package edu.umass.cs.ciir.waltz.postings.docset;

import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.io.Directory;
import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.WaltzDiskMap;
import edu.umass.cs.ciir.waltz.coders.files.DataSink;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.sorter.ExternalSortingWriter;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author jfoley
 */
public class DocumentSetWriter<K> implements Closeable {
  private final StreamingDocumentSetChunkWriter<K> writer;
  private final TemporaryDirectory tmpdir;
  private final ExternalSortingWriter<DocumentSetChunk<K>> sorter;

  public DocumentSetWriter(Coder<K> keyCoder, Directory baseDir, String baseName) throws IOException {
    this.tmpdir = new TemporaryDirectory();
    this.sorter = new ExternalSortingWriter<>(
        tmpdir.get(),
        new DocumentSetChunkReducer<>(),
        new DocumentSetChunkCoder<>(keyCoder));
    this.writer = new StreamingDocumentSetChunkWriter<>(
        new WaltzDiskMap.Writer<>(baseDir, baseName, keyCoder, null)
    );
  }

  public void process(K item, int documentId) throws IOException {
    sorter.process(new DocumentSetChunk<>(item, documentId));
  }

  @Override
  public void close() throws IOException {
    sorter.close();
    for (DocumentSetChunk<K> current : sorter.getOutput()) {
      writer.process(current);
    }
    writer.close();
    tmpdir.close(); // delete files
  }

  public static class DeltaGappingIntWriter implements Closeable {
    final DataSink output;
    int previous = 0;
    int total = 0;

    public DeltaGappingIntWriter(DataSink output) {
      this.output = output;
      reset();
    }
    public void reset() {
      total = 0;
      previous = 0;
    }
    public void write(int x) throws IOException {
      total++;
      int delta = x - previous;
      output.write(VarUInt.instance, delta);
      previous = x;
    }

    public void write(IntList docs) throws IOException {
      for (int doc : docs) {
        write(doc);
      }
    }

    public void close() {
    }

    // flush current stream of ints.
    public void flush() {

    }
  }
}
