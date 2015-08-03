package edu.umass.cs.ciir.waltz.sys;

import ciir.jfoley.chai.io.Directory;
import ciir.jfoley.chai.io.IO;
import ciir.jfoley.chai.jvm.MemoryNotifier;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.files.DataSink;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMapWriter;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.io.postings.format.BlockedPostingValueWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

/**
 * @author jfoley
 */
public class PostingIndex {
  public interface KeyMetadata<V,Subclass extends KeyMetadata<V, ?>> {
    int totalDocuments();
    void accumulate(Subclass m);
    void accumulate(int document, V item);
    Subclass zero();
  }

  public static final class PostingIndexEntry<K,M,V> implements Comparable<PostingIndexEntry<K,M,V>> {
    private Comparator<K> keyCmp;
    private K key;
    private M metadata;
    private PostingMover<V> values;

    @Override
    public int compareTo(@Nonnull PostingIndexEntry<K,M,V> o) {
      int cmp = keyCmp.compare(this.key, o.key);
      if(cmp != 0) return cmp;
      return Integer.compare(values.currentKey(), o.values.currentKey());
    }
  }

  public interface PostingIndexReader<K,M,V> {
    @Nonnull Comparator<K> getKeyCmp();
    @Nonnull Iterable<PostingIndexEntry<K,M,V>> items();
  }
  public interface PostingIndexWriter<K,M,V> extends Closeable {
    void writeNewKey(K key) throws IOException;
    void writeMetadata(M metadata) throws IOException;
    void writePosting(int doc, V value) throws IOException;
  }
  public static final class TemporaryPosting<M extends KeyMetadata<V,M>,V> {
    public final PostingsConfig<?,M,V> cfg;
    private M metadata;
    public ByteArrayOutputStream data;
    private int previousDocument;

    public TemporaryPosting(PostingsConfig<?,M,V> cfg) {
      this.cfg = cfg;
      this.data = new ByteArrayOutputStream();
      this.previousDocument = 0;
      this.metadata = cfg.newMetadata();
    }

    public void add(int document, V payload) {
      assert(previousDocument == 0 || document > previousDocument);
      metadata.accumulate(document, payload);
      VarUInt.instance.writePrim(data, document - previousDocument);
      cfg.valCoder.write(data, payload);
      previousDocument = document;
    }

    public TemporaryPosting<M,V> write(OutputStream out) throws IOException {
      cfg.metadataCoder.write(out, metadata);
      data.writeTo(out);
      return this;
    }
    public void close() throws IOException {
      this.data = null;
    }
  }
  public static final class TmpStreamPostingIndexWriter<K,M extends KeyMetadata<V,M>,V> implements Flushable, Closeable {
    private final Directory tmpDir;
    public int temporaryIndex;
    PostingsConfig<K,M,V> cfg;
    public final TreeMap<K, TemporaryPosting<M,V>> memoryPostingIndex;
    private int totalDocuments;

    public TmpStreamPostingIndexWriter(Directory outputDir, String baseName, PostingsConfig<K,M,V> cfg) {
      this.tmpDir = outputDir.childDir(baseName+".tmp");
      this.cfg = cfg;
      this.memoryPostingIndex = new TreeMap<>(cfg.keyCmp);
      this.totalDocuments = 0;
    }

    public synchronized int addDocument() {
      return this.totalDocuments++;
    }

    public synchronized void add(K key, int document, V payload) {
      TemporaryPosting<M, V> valBuilder = memoryPostingIndex.get(key);
      if (valBuilder == null) {
        valBuilder = new TemporaryPosting<>(cfg);
        memoryPostingIndex.put(key, valBuilder);
      }
      valBuilder.add(document, payload);
    }

    public TmpPostingMerger<K,M,V> getMerger(List<Integer> ids) throws IOException {
      List<InputStream> inputs = new ArrayList<>();
      for (Integer id : ids) {
        assert(id < temporaryIndex);
        File input = getOutput(id);
        inputs.add(IO.openInputStream(input));
      }
      return new TmpPostingMerger<>(cfg, inputs);
    }

    public File getOutput(int id) {
      return tmpDir.child(Integer.toString(id)+".lzf");
    }

    @Override
    public synchronized void flush() throws IOException {
      if(memoryPostingIndex.isEmpty()) return;

      File output = getOutput(temporaryIndex++);
      try (OutputStream segmentWriter = IO.openOutputStream(output)) {
        // count
        int keyCount = memoryPostingIndex.size();
        VarUInt.instance.writePrim(segmentWriter, keyCount);
        VarUInt.instance.writePrim(segmentWriter, totalDocuments);

        // followed by k,v pairs in order:
        for (Map.Entry<K, TemporaryPosting<M,V>> kv : memoryPostingIndex.entrySet()) {
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
  }

  public static final class PostingsConfig<K,M extends KeyMetadata<V,M>, V> {
    public final Coder<K> keyCoder;
    public final Coder<M> metadataCoder;
    public final Coder<V> valCoder;
    public final Comparator<K> keyCmp;
    private final KeyMetadata<V,M> metadata;

    public PostingsConfig(Coder<K> keyCoder, Coder<M> metadataCoder, Coder<V> valCoder, Comparator<K> keyCmp, M metadata) {
      this.keyCoder = keyCoder.lengthSafe();
      this.metadataCoder = metadataCoder.lengthSafe();
      this.valCoder = valCoder.lengthSafe();
      this.keyCmp = keyCmp;
      this.metadata = metadata;
    }

    public M newMetadata() {
      return metadata.zero();
    }
  }

  public static final class TmpPostingReader<K,M extends KeyMetadata<V,M>, V> implements Comparable<TmpPostingReader<K,M,V>> {
    final PostingsConfig<K,M,V> cfg;
    final InputStream input;
    final int keyCount;
    private final int totalDocuments;
    int keyIndex;
    K currentKey;
    M currentMetadata;
    int documentCount;
    int documentIndex;
    int currentDocument;
    private V currentValue;

    public TmpPostingReader(PostingsConfig<K,M,V> cfg, InputStream input) {
      this.cfg = cfg;
      this.input = input;
      this.keyCount = VarUInt.instance.read(input);
      this.totalDocuments = VarUInt.instance.read(input);
      this.getNextKey();
    }
    public int getTotalDocuments() {
      return totalDocuments;
    }

    public M getCurrentMetadata() {
      return currentMetadata;
    }
    public boolean hasNextDocument() {
      return documentIndex < documentCount;
    }
    public boolean hasNextKey() {
      return keyIndex < keyCount;
    }
    @Nullable
    public K getNextKey() {
      while(hasNextDocument()) {
        readNext();
      }
      if(hasNextKey()) {
        currentKey = cfg.keyCoder.read(input);
        keyIndex++;
        documentIndex = 0;
        currentDocument = 0;
        currentMetadata = cfg.metadataCoder.read(input);
        documentCount = currentMetadata.totalDocuments();
        readNext();
        return currentKey;
      }
      currentKey = null;
      return null;
    }

    public void readNext() {
      assert(hasNextDocument());
      // read delta:
      currentDocument += VarUInt.instance.read(input);
      currentValue = cfg.valCoder.read(input);
      this.documentIndex++;
    }

    @Override
    public int compareTo(@Nonnull TmpPostingReader<K,M,V> o) {
      int cmp = cfg.keyCmp.compare(currentKey, o.currentKey);
      if(cmp != 0) return cmp;
      return Integer.compare(currentDocument, o.currentDocument);
    }
  }

  public static final class TmpPostingMerger<K,M extends KeyMetadata<V,M>,V> {
    public final PostingsConfig<K,M,V> cfg;
    public final PriorityQueue<TmpPostingReader<K,M,V>> queue;

    public TmpPostingMerger(PostingsConfig<K,M,V> cfg, List<InputStream> sources) {
      this.cfg = cfg;
      queue = new PriorityQueue<>(sources.size());

      for (int i = 0; i < sources.size(); i++) {
        InputStream source = sources.get(i);
        TmpPostingReader<K, M, V> reader = new TmpPostingReader<>(cfg, source);
        queue.offer(reader);
      }
    }

    public void write(PostingIndexWriter<K,M,V> writer) throws IOException {

      while(!queue.isEmpty()) {
        K key = queue.peek().currentKey;
        if(key == null) {
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
          writer.writePosting(reader.currentDocument, reader.currentValue);
          while(reader.hasNextDocument()) {
            reader.readNext();
            writer.writePosting(reader.currentDocument, reader.currentValue);
          }
          // move past the current key and put back in the queue
          K next = reader.getNextKey();
          if(next != null) {
            queue.offer(reader);
          }
        }
      }
    }

  }

  public static class BlockedPostingsWriter<K, M extends KeyMetadata<V,M>, V> implements PostingIndexWriter<K,M,V> {
    public final WaltzDiskMapWriter<K, PostingMover<V>> writer;
    private final PostingsConfig<K, M, V> cfg;
    private BlockedPostingValueWriter<V> postingsWriter;
    DataSink valueWriter;

    public BlockedPostingsWriter(PostingsConfig<K, M, V> cfg, WaltzDiskMapWriter<K, PostingMover<V>> writer) {
      this.cfg = cfg;
      this.writer = writer;
      this.valueWriter = writer.valueWriter();
      this.postingsWriter = null;
    }
    public BlockedPostingsWriter(PostingsConfig<K,M,V> cfg, Directory outputDir, String baseName) throws IOException {
      this(cfg, new WaltzDiskMapWriter<>(outputDir, baseName, cfg.keyCoder, null, false));
    }

    @Override
    public void writeNewKey(K key) throws IOException {
      finishCurrentPostingList();
      writer.beginWrite(key);
    }

    @Override
    public void writeMetadata(M metadata) throws IOException {
      assert(postingsWriter == null);
      writer.valueWriter().write(VarUInt.instance, metadata.totalDocuments());
      postingsWriter = new BlockedPostingValueWriter<V>(valueWriter, cfg.valCoder);
    }

    @Override
    public void writePosting(int doc, V value) throws IOException {
      assert(postingsWriter != null);
      postingsWriter.add(doc, value);
    }

    public void finishCurrentPostingList() throws IOException {
      if(postingsWriter != null) {
        postingsWriter.finish();
        postingsWriter = null;
      }
    }

    @Override
    public void close() throws IOException {
      finishCurrentPostingList();
      writer.close();
    }
  }
}
