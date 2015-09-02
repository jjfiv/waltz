package edu.umass.cs.ciir.waltz.sys.tmp;

import ciir.jfoley.chai.collections.util.QuickSort;
import ciir.jfoley.chai.io.IO;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
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
public class TmpPostingWriter<K,V> implements Closeable {
  public final PostingsConfig<K,V> cfg;
  private final OutputStream output;

  public TmpPostingWriter(PostingsConfig<K, V> cfg, File output) throws IOException {
    this.cfg = cfg;
    this.output = IO.openOutputStream(output);
  }

  public void writeHeader(int totalDocuments, int keyCount) {
    VarUInt.instance.writePrim(output, keyCount);
    VarUInt.instance.writePrim(output, totalDocuments);
  }

  private void writeEntry(K key, TemporaryPosting<V> values) throws IOException {
    cfg.keyCoder.write(output, key);
    values.write(output);
    values.close();
  }

  public void writeUnsorted(int totalDocuments, List<Map.Entry<K,TemporaryPosting<V>>> data) throws IOException {
    // sort data:
    QuickSort.sort(
        (lhs, rhs) -> cfg.keyCmp.compare(lhs.getKey(), rhs.getKey()), data
    );
    writeSorted(totalDocuments, data);
  }
  public void writeSorted(int totalDocuments, List<Map.Entry<K,TemporaryPosting<V>>> data) throws IOException {
    // count
    int keyCount = data.size();
    writeHeader(totalDocuments, keyCount);

    // followed by k,v pairs in order:
    for (Map.Entry<K, TemporaryPosting<V>> kv : data) {
      writeEntry(kv.getKey(), kv.getValue());
    }
  }

  @Override
  public void close() throws IOException {
    this.output.close();
  }
}
