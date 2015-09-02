package edu.umass.cs.ciir.waltz.sys.tmp;

import edu.umass.cs.ciir.waltz.sys.KeyMetadata;
import edu.umass.cs.ciir.waltz.sys.PostingIndexWriter;
import edu.umass.cs.ciir.waltz.sys.PostingsConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author jfoley
 */
public final class TmpPostingMerger<K, V> {
  public final PostingsConfig<K, V> cfg;
  public final PriorityQueue<TmpPostingReader<K, V>> queue;

  public TmpPostingMerger(PostingsConfig<K, V> cfg, List<TmpPostingReader<K,V>> sources) {
    this.cfg = cfg;
    queue = new PriorityQueue<>(sources);
  }

  public void write(PostingIndexWriter<K,V> writer) throws IOException {

    int totalDocumentCount = 0;
    for (TmpPostingReader<K, V> reader : queue) {
      totalDocumentCount += reader.getTotalDocuments();
    }
    writer.writeHeader(totalDocumentCount);

    while (!queue.isEmpty()) {
      K key = queue.peek().currentKey;
      if (key == null) {
        queue.poll();
        continue;
      }

      // collect all indices that have the current key
      List<TmpPostingReader<K, V>> matching = new ArrayList<>();
      while (!queue.isEmpty() && key.equals(queue.peek().currentKey)) {
        TmpPostingReader<K, V> reader = queue.poll();
        matching.add(reader);
      }

      // sum up metadata:
      KeyMetadata<V> totalMeta = cfg.newMetadata();
      for (TmpPostingReader<K, V> reader : matching) {
        totalMeta.accumulate(reader.getCurrentMetadata());
      }
      writer.writeNewKey(key, totalMeta);

      // write posting list:
      for (TmpPostingReader<K, V> reader : matching) {
        // could possibly replace this with a blit if target is intermediate as well:
        writer.writePosting(reader.currentDocument, reader.getCurrentValue());
        while (reader.hasNextDocument()) {
          reader.readNext();
          writer.writePosting(reader.currentDocument, reader.getCurrentValue());
        }
        // move past the current key and put back in the queue
        K next = reader.getNextKey();
        if (next != null) {
          queue.offer(reader);
        }
      }
    }
  }
}
