package edu.umass.cs.ciir.waltz.sys;

import ciir.jfoley.chai.io.Directory;
import ciir.jfoley.chai.jvm.MemoryNotifier;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.files.DataSink;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapWriter;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;

import javax.annotation.Nonnull;
import java.io.Flushable;
import java.io.IOException;
import java.util.*;

/**
 * @author jfoley
 */
public class PostingIndex {
  public interface KeyMetadata<V> {
    int totalKeys();
    void accumulate(KeyMetadata<?> m);
    void accumulate(int document, V item);
    KeyMetadata<V> zero();
  }

  public static final class PostingIndexEntry<K,V> implements Comparable<PostingIndexEntry<K,V>> {
    private Comparator<K> keyCmp;
    private K key;
    private KeyMetadata<V> metadata;
    private PostingMover<V> values;

    @Override
    public int compareTo(@Nonnull PostingIndexEntry<K, V> o) {
      int cmp = keyCmp.compare(this.key, o.key);
      if(cmp != 0) return cmp;
      return Integer.compare(values.currentKey(), o.values.currentKey());
    }
  }

  public interface PostingIndexReader<K,V> {
    @Nonnull Comparator<K> getKeyCmp();
    @Nonnull Iterable<PostingIndexEntry<K,V>> items();
  }
  public interface PostingIndexWriter<K,V> {
    @Nonnull Comparator<K> getKeyCmp();
    void writeEntry(@Nonnull PostingIndexEntry<K,V> key);
    void writeMergedEntry(@Nonnull List<PostingIndexEntry<K,V>> keys);
  }
  public static final class TemporaryPosting<V> {
    public final Coder<V> coder;
    public final Coder<KeyMetadata<V>> metaCoder;
    public final KeyMetadata<V> metadata;
    public final ByteBuilder data;

    public TemporaryPosting(Coder<V> valueCoder, Coder<KeyMetadata<V>> metaCoder, KeyMetadata<V> zero) {
      this.metaCoder = metaCoder;
      this.data = new ByteBuilder();
      this.metadata = zero;
      this.coder = valueCoder;
    }

    public void add(int document, V payload) {
      metadata.accumulate(document, payload);
      VarUInt.instance.write(document);
      coder.write(data.asOutputStream(), payload);
    }

    public TemporaryPosting<V> write(DataSink out) throws IOException {
      out.write(metaCoder, metadata);
      out.write(data);
      return this;
    }
    public void close() throws IOException {
      this.data.close();
    }
  }
  public static final class TmpStreamPostingIndexWriter<K,V> implements Flushable {
    private final Directory tmpDir;
    public int temporaryIndex;
    public final Coder<K> keyCoder;
    public final Comparator<K> keyCmp;
    public final Coder<V> valueCoder;
    public final KeyMetadata<V> metadata;
    public final Coder<KeyMetadata<V>> metaCoder;
    public final TreeMap<K, TemporaryPosting<V>> memoryPostingIndex;

    public TmpStreamPostingIndexWriter(Directory outputDir, String baseName, Coder<K> keyCoder, Comparator<K> keyCmp, Coder<V> valueCoder, KeyMetadata<V> metadata, Coder<KeyMetadata<V>> metaCoder) {
      this.tmpDir = outputDir.childDir(baseName+".tmp");
      this.keyCoder = keyCoder;
      this.keyCmp = keyCmp;
      this.valueCoder = valueCoder;
      this.metadata = metadata;
      this.metaCoder = metaCoder;
      this.memoryPostingIndex = new TreeMap<>(keyCmp);
    }

    public synchronized void add(K key, int document, V payload) {
      TemporaryPosting<V> valBuilder = memoryPostingIndex.get(key);
      if(valBuilder == null) {
        valBuilder = new TemporaryPosting<>(valueCoder, metaCoder, metadata.zero());
        valBuilder.add(document, payload);
      }
    }

    public Directory getSegmentDir() {
      return tmpDir;
    }

    // TODO: return better
    public List<PostingIndexReader<K,V>> getSegments() {
      return Collections.emptyList();
    }

    @Override
    public synchronized void flush() throws IOException {
      WaltzDiskMapWriter<K, ?> segmentWriter = new WaltzDiskMapWriter<>(
          tmpDir, Integer.toString(temporaryIndex++),
          keyCoder,
          null, false // don't encode values for us and don't sort.
          );

      for (Map.Entry<K, TemporaryPosting<V>> kv : memoryPostingIndex.entrySet()) {
        // send key:
        segmentWriter.beginWrite(kv.getKey());
        // write value direct:
        kv.getValue().write(segmentWriter.valueWriter()).close();
      }
      // clear in-memory map:
      memoryPostingIndex.clear();
    }

    public void close() throws IOException {
      MemoryNotifier.unregister(this);
      flush();
    }
  }

  public static final class TmpPostingIndexReader<K,V> implements PostingIndexReader<K,V> {

    @Nonnull
    @Override
    public Comparator<K> getKeyCmp() {
      return null;
    }

    @Nonnull
    @Override
    public Iterable<PostingIndexEntry<K, V>> items() {
      return null;
    }
  }

}
