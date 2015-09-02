package edu.umass.cs.ciir.waltz.sys.tmp;

import edu.umass.cs.ciir.waltz.sys.PostingsConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jfoley
 */
public class MemoryPostingIndex<K,V> {
  public final PostingsConfig<K,V> cfg;
  public final HashMap<K, TemporaryPosting<V>> memoryPostingIndex;
  public int totalDocuments;

  public MemoryPostingIndex(PostingsConfig<K, V> cfg) {
    this.cfg = cfg;
    this.memoryPostingIndex = new HashMap<>();
    totalDocuments = 0;
  }

  public void add(K key, int document, V payload) {
    memoryPostingIndex
        .computeIfAbsent(key, (k) -> new TemporaryPosting<>(cfg))
        .add(document, payload);
  }

  public boolean isEmpty() {
    return memoryPostingIndex.isEmpty();
  }
  public int size() {
    return memoryPostingIndex.size();
  }
  public int getTotalDocuments() {
    return totalDocuments;
  }

  public void reset() {
    this.totalDocuments = 0;
    this.memoryPostingIndex.clear();
  }

  public ArrayList<Map.Entry<K,TemporaryPosting<V>>> readDestructively() {
    ArrayList<Map.Entry<K, TemporaryPosting<V>>> data = new ArrayList<>(memoryPostingIndex.entrySet());
    memoryPostingIndex.clear();
    return data;
  }

  public void write(TmpPostingWriter<K,V> output) throws IOException {
    output.writeUnsorted(totalDocuments, readDestructively());
  }

}
