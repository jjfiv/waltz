package edu.umass.cs.ciir.waltz.sys.tmp;

import ciir.jfoley.chai.collections.IntRange;
import ciir.jfoley.chai.collections.util.QuickSort;
import ciir.jfoley.chai.io.Directory;
import ciir.jfoley.chai.io.IO;
import ciir.jfoley.chai.jvm.MemoryNotifier;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.sys.PostingIndexWriter;
import edu.umass.cs.ciir.waltz.sys.PostingsConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley
 */
public final class TmpStreamPostingIndexWriter<K, V> implements Flushable, Closeable {
  private final Directory tmpDir;
  public int temporaryIndex;
  PostingsConfig<K, V> cfg;
  public final HashMap<K, TemporaryPosting<V>> memoryPostingIndex;
  private int totalDocuments;
  private int flushSize = 200000;

  public TmpStreamPostingIndexWriter(Directory outputDir, String baseName, PostingsConfig<K, V> cfg) {
    this.tmpDir = outputDir.childDir(baseName + ".tmp");
    this.cfg = cfg;
    this.memoryPostingIndex = new HashMap<>(flushSize);
    this.totalDocuments = 0;
  }

  public synchronized int addDocument() {
    return this.totalDocuments++;
  }

  public synchronized void add(K key, int document, V payload) {
    TemporaryPosting<V> valBuilder = memoryPostingIndex.get(key);
    if (valBuilder == null) {
      valBuilder = new TemporaryPosting<>(cfg);
      memoryPostingIndex.put(key, valBuilder);
    }
    valBuilder.add(document, payload);

    if (memoryPostingIndex.size() > flushSize) {
      try {
        flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public TmpPostingMerger<K, V> getMerger(List<Integer> ids) throws IOException {
    List<InputStream> inputs = new ArrayList<>();
    for (Integer id : ids) {
      assert (id < temporaryIndex);
      File input = getOutput(id);
      inputs.add(IO.openInputStream(input));
    }
    return new TmpPostingMerger<>(cfg, inputs);
  }

  public File getOutput(int id) {
    return tmpDir.child(Integer.toString(id) + ".lzf");
  }

  @Override
  public synchronized void flush() throws IOException {
    if (memoryPostingIndex.isEmpty()) return;

    File output = getOutput(temporaryIndex++);
    try (OutputStream segmentWriter = IO.openOutputStream(output)) {
      // count
      int keyCount = memoryPostingIndex.size();
      VarUInt.instance.writePrim(segmentWriter, keyCount);
      VarUInt.instance.writePrim(segmentWriter, totalDocuments);

      List<Map.Entry<K, TemporaryPosting<V>>> data = new ArrayList<>(memoryPostingIndex.entrySet());
      memoryPostingIndex.clear();
      QuickSort.sort(
          (lhs, rhs) -> cfg.keyCmp.compare(lhs.getKey(), rhs.getKey()), data
      );

      // followed by k,v pairs in order:
      for (Map.Entry<K, TemporaryPosting<V>> kv : data) {
        cfg.keyCoder.write(segmentWriter, kv.getKey());
        kv.getValue().write(segmentWriter);
        kv.getValue().close();
      }
    }
    // clear in-memory map:
    memoryPostingIndex.clear();
  }

  public void close() throws IOException {
    MemoryNotifier.unregister(this);
    flush();
  }

  public void mergeTo(PostingIndexWriter<K, V> finalWriter) throws IOException {
    close();
    TmpPostingMerger<K, V> merger = this.getMerger(IntRange.exclusive(0, temporaryIndex));
    merger.write(finalWriter);
  }
}
