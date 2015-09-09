package edu.umass.cs.ciir.waltz.sys.tmp;

import ciir.jfoley.chai.collections.util.QuickSort;
import ciir.jfoley.chai.io.IO;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.sys.KeyMetadata;
import edu.umass.cs.ciir.waltz.sys.PostingIndexWriter;
import edu.umass.cs.ciir.waltz.sys.PostingsConfig;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley
 */
public class TmpPostingWriter<K,V> implements PostingIndexWriter<K,V>, Closeable {
  public final PostingsConfig<K,V> cfg;
  private final OutputStream output;
  private int previousDocument;

  public TmpPostingWriter(PostingsConfig<K, V> cfg, File output) throws IOException {
    this.cfg = cfg;
    this.output = IO.openOutputStream(output);
  }

  public void writeEntry(K key, TemporaryPosting<V> values) throws IOException {
    cfg.keyCoder.write(output, key);
    values.write(output);
    values.close();
  }

  public void writeUnsorted(int totalDocuments, List<Map.Entry<K,TemporaryPosting<V>>> data) throws IOException {
    // sort data:
    QuickSort.sort(
        (lhs, rhs) -> cfg.keyCmp.compare(lhs.getKey(), rhs.getKey()),
        data
    );
    /*Collections.sort(
        data,
        (lhs, rhs) -> cfg.keyCmp.compare(lhs.getKey(), rhs.getKey())
    );*/
    writeSorted(totalDocuments, data);
  }
  public void writeSorted(int totalDocuments, List<Map.Entry<K,TemporaryPosting<V>>> data) throws IOException {
    // count
    writeHeader(totalDocuments);

    // followed by k,v pairs in order:
    for (Map.Entry<K, TemporaryPosting<V>> kv : data) {
      writeEntry(kv.getKey(), kv.getValue());
    }
  }

  @Override
  public void close() throws IOException {
    this.output.close();
  }

  @Override
  public void writeNewKey(K key, KeyMetadata<V> metadata) throws IOException {
    cfg.keyCoder.write(output, key);
    previousDocument = 0;
    metadata.encode().write(output);
  }

  @Override
  public void writePosting(int document, V value) throws IOException {
    VarUInt.instance.writePrim(output, document - previousDocument);
    cfg.valCoder.write(output, value);
    previousDocument = document;
  }

  @Override
  public void writeHeader(int totalDocumentCount) {
    VarUInt.instance.writePrim(output, totalDocumentCount);
  }
}
