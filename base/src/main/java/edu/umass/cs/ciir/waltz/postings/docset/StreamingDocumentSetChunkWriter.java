package edu.umass.cs.ciir.waltz.postings.docset;

import edu.umass.cs.ciir.waltz.coders.files.DataSink;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapWriter;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

/**
 * @author jfoley
 */
public class StreamingDocumentSetChunkWriter<K> implements Closeable {
  public int previous;
  public long valueStartOffset;
  public K previousKey;
  public final WaltzDiskMapWriter<K, ?> writer;
  public DocumentSetWriter.DeltaGappingIntWriter intOutput;
  public DataSink valueWriter;

  public StreamingDocumentSetChunkWriter(WaltzDiskMapWriter<K, ?> writer) {
    this.writer = writer;
    this.valueWriter = writer.valueWriter();
    this.intOutput = new DocumentSetWriter.DeltaGappingIntWriter(valueWriter);
  }

  public void process(DocumentSetChunk<K> chunk) throws IOException {
    if (previousKey == null || !Objects.equals(chunk.key, previousKey)) {
      finishPreviousValue();
      startNewValue(chunk.key);
      previousKey = chunk.key;
    }
    intOutput.write(chunk.docs);
  }

  private void startNewValue(K key) throws IOException {
    writer.beginWrite(key);
    valueStartOffset = valueWriter.tell();
    intOutput.reset();
    // will rewrite over this later; negative will at least trigger concern if we forget:
    valueWriter.write(FixedSize.ints, 0xdeadbeef);
  }

  public void finishPreviousValue() throws IOException {
    intOutput.flush();
    if (previousKey == null) return;
    valueWriter.write(valueStartOffset, FixedSize.ints, intOutput.total);
    writer.finishWrite();
  }

  public void close() throws IOException {
    finishPreviousValue();
    intOutput.close();
    writer.close();
  }
}
