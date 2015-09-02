package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.collections.util.MapFns;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jfoley
 */
public class GeometricItemMerger {
  public static final ExecutorService asyncExec = ForkJoinPool.commonPool();
  public static final int DEFAULT_MERGE_FACTOR = 10;
  public static final boolean useThreads = true; // almost 2x slower building positions on robust without this

  final MergeFn mergeFn;
  // Logical size only: 0: [...], 1: [...] ... whenever we get 10 level 0s we upgrade them to a level 1... etc.
  final Map<Integer, List<Integer>> itemsByLevel;
  AtomicInteger liveJobs;
  protected final int mergeFactor;
  private int nextId;

  public GeometricItemMerger(int mergeFactor, MergeFn mergeFn) {
    this.mergeFactor = mergeFactor;
    nextId = 0;
    liveJobs = new AtomicInteger(0);
    itemsByLevel = new ConcurrentHashMap<>();
    this.mergeFn = mergeFn;
  }

  public synchronized void doAsync(Runnable r) {
    liveJobs.incrementAndGet();
    if(useThreads) {
      asyncExec.submit(() -> {
        r.run();
        liveJobs.decrementAndGet();
      });
    } else {
      r.run();
      liveJobs.decrementAndGet();
    }
  }

  public synchronized int allocate() {
    return nextId++;
  }

  public synchronized void insert(int id, int level)  {
    MapFns.extendListInMap(itemsByLevel, level, id);
  }

  public void addNewItem(int itemId) {
    insert(itemId, 0);
  }

  public void close() throws IOException {
    finish();
  }

  public void finish() throws IOException {
    do {
      checkIfWeCanMergeItems();
      try {
        Thread.sleep(100);
      } catch (InterruptedException ignored) { }
    } while(liveJobs.get() > 0);

  }

  public void waitForCurrentJobs() {
    while(liveJobs.get() > 0) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ignored) { }
    }
  }

  public IntList getFinalItems() throws IOException {
    finish();
    IntList total = new IntList();
    for (List<Integer> liveIds : itemsByLevel.values()) {
      total.addAll(liveIds);
    }
    return total;
  }

  public synchronized void checkIfWeCanMergeItems() throws IOException {
    while(true) {
      boolean changed = false;

      for (Map.Entry<Integer, List<Integer>> kv : itemsByLevel.entrySet()) {
        int level = kv.getKey();
        List<Integer> runs = kv.getValue();
        if (runs.size() >= mergeFactor) {
          itemsByLevel.remove(level);

          doAsync(() -> {
            try {
              int newId = allocate();
              mergeFn.apply(runs, newId);
              insert(newId, level+1);
            } catch (Throwable e) {
              e.printStackTrace(System.err);
              throw new RuntimeException(e);
            }
          });
          changed = true;
          break;
        }
      }
      if(!changed) break;
    }
  }

  public interface MergeFn {
    void apply(List<Integer> inputs, int output) throws IOException;
  }

}
