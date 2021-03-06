package edu.umass.cs.ciir.waltz.sys.tmp;

import ciir.jfoley.chai.io.Directory;
import ciir.jfoley.chai.io.IO;
import ciir.jfoley.chai.jvm.MemoryNotifier;
import edu.umass.cs.ciir.waltz.coders.sorter.GeometricItemMerger;
import edu.umass.cs.ciir.waltz.sys.PostingIndexWriter;
import edu.umass.cs.ciir.waltz.sys.PostingsConfig;

import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author jfoley
 */
public final class TmpStreamPostingIndexWriter<K, V> implements Flushable, Closeable {
  private final Directory tmpDir;
  PostingsConfig<K, V> cfg;
  public MemoryPostingIndex<K,V> tmpIndex;
  private int totalDocuments;
  private int flushSize = 200000;
  GeometricItemMerger merger;

  public TmpStreamPostingIndexWriter(Directory outputDir, String baseName, PostingsConfig<K, V> cfg) {
    this.tmpDir = outputDir.childDir(baseName + ".tmp");
    this.cfg = cfg;
    tmpIndex = new MemoryPostingIndex<>(cfg);
    this.totalDocuments = 0;
    this.merger = new GeometricItemMerger(8, new MergeIntermediate());
    MemoryNotifier.register(this);
  }

  public synchronized int addDocument() {
    // local doc number
    this.tmpIndex.totalDocuments++;
    // global doc number
    return totalDocuments++;
  }

  public synchronized void add(MemoryPostingIndex<K,V> memIndex) {
    int currentId = merger.allocate();
    File output = getOutput(currentId);

    // flush to disk asynchronously
    merger.doAsync(() -> {
      try (TmpPostingWriter<K, V> writer = new TmpPostingWriter<>(cfg, output)) {
        memIndex.write(writer);
      } catch (IOException e) {
        e.printStackTrace();
      }
      merger.addNewItem(currentId);
    });
  }


  public synchronized void add(K key, int document, V payload) {
    tmpIndex.add(key, document, payload);

    if (tmpIndex.size() > flushSize) {
      try {
        flush();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public TmpPostingMerger<K, V> getMerger(List<Integer> ids) throws IOException {
    List<TmpPostingReader<K,V>> inputs = new ArrayList<>();
    for (Integer id : ids) {
      File input = getOutput(id);
      TmpPostingReader<K,V> x = new TmpPostingReader<>(cfg, IO.openInputStream(input));
      inputs.add(x);
    }
    return new TmpPostingMerger<>(cfg, inputs);
  }

  public File getOutput(int id) {
    return tmpDir.child(Integer.toString(id) + ".lzf");
  }

  @Override
  public void flush() throws IOException {
    synchronized (this) {
      if (tmpIndex.isEmpty()) {
        return;
      }

      MemoryPostingIndex<K, V> prev = tmpIndex;
      tmpIndex = new MemoryPostingIndex<>(cfg);
      int currentId = merger.allocate();
      File output = getOutput(currentId);

      // flush to disk asynchronously
      merger.doAsync(() -> {
        try (TmpPostingWriter<K, V> writer = new TmpPostingWriter<>(cfg, output)) {
          prev.write(writer);
        } catch (IOException e) {
          e.printStackTrace();
        }
        merger.addNewItem(currentId);
      });
    }

    merger.checkIfWeCanMergeItems();
  }

  public void close() throws IOException {
    MemoryNotifier.unregister(this);
    flush();
  }

  public final class MergeIntermediate implements GeometricItemMerger.MergeFn {
    @Override
    public void apply(List<Integer> inputs, int output) throws IOException {
      TmpPostingMerger<K, V> merger = getMerger(inputs);
      try (TmpPostingWriter<K,V> writer = new TmpPostingWriter<>(cfg, getOutput(output))) {
        merger.write(writer);
      }
      for (int input : inputs) {
        if(!getOutput(input).delete()) {
          Logger.getAnonymousLogger().warning("Couldn't delete temporary indexing file: "+getOutput(input).getAbsolutePath());
        }
      }
    }
  }

  public void mergeTo(PostingIndexWriter<K, V> finalWriter) throws IOException {
    flush();
    TmpPostingMerger<K, V> postingMerger = this.getMerger(merger.getFinalItems());
    postingMerger.write(finalWriter);
  }
}
