package edu.umass.cs.ciir.waltz.sys.tmp;

import edu.umass.cs.ciir.waltz.sys.KeyMetadata;
import edu.umass.cs.ciir.waltz.sys.PostingIndexWriter;
import edu.umass.cs.ciir.waltz.sys.PostingsConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author jfoley
 */
public final class TmpPostingMerger<K, M extends KeyMetadata<V, M>, V> {
  public final PostingsConfig<K, M, V> cfg;
  public final PriorityQueue<TmpPostingReader<K, M, V>> queue;

  public TmpPostingMerger(PostingsConfig<K, M, V> cfg, List<InputStream> sources) {
    this.cfg = cfg;
    queue = new PriorityQueue<>(sources.size());

    for (InputStream source : sources) {
      TmpPostingReader<K, M, V> reader = new TmpPostingReader<>(cfg, source);
      queue.offer(reader);
    }
  }

  public void write(PostingIndexWriter<K, M, V> writer) throws IOException {

    while (!queue.isEmpty()) {
      K key = queue.peek().currentKey;
      if (key == null) {
        queue.poll();
        continue;
      }

      writer.writeNewKey(key);

      // collect all indices that have the current key
      List<TmpPostingReader<K, M, V>> matching = new ArrayList<>();
      while (!queue.isEmpty() && key.equals(queue.peek().currentKey)) {
        TmpPostingReader<K, M, V> reader = queue.poll();
        matching.add(reader);
      }

      // sum up metadata:
      M totalMeta = cfg.newMetadata();
      for (TmpPostingReader<K, M, V> reader : matching) {
        totalMeta.accumulate(reader.getCurrentMetadata());
      }
      writer.writeMetadata(totalMeta);

      // write posting list:
      for (TmpPostingReader<K, M, V> reader : matching) {
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
