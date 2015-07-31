package edu.umass.cs.ciir.waltz.coders.sorter;

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
public abstract class GeometricItemMerger {
  public static final ExecutorService asyncExec = ForkJoinPool.commonPool();
  public static final int DEFAULT_MERGE_FACTOR = 10;

  final Map<Integer, List<Integer>> runsByLevel;
  AtomicInteger liveJobs;
  protected final int mergeFactor;
  private int nextId;

  public GeometricItemMerger(int mergeFactor) {
    this.mergeFactor = mergeFactor;
    nextId = 0;
    liveJobs = new AtomicInteger(0);
    runsByLevel = new ConcurrentHashMap<>();
  }

  public synchronized void doAsync(Runnable r) {
    liveJobs.incrementAndGet();
    asyncExec.submit(() -> {
      r.run();
      liveJobs.decrementAndGet();
    });
  }

  public synchronized int allocate() {
    return nextId++;
  }

  public synchronized void addNewRun(int runId, int level)  {
    MapFns.extendListInMap(runsByLevel, level, runId);
  }

  public void stop() throws IOException {
    // merge as many runs as possible.
    do {
      checkIfWeCanMergeRuns();
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

  public synchronized void checkIfWeCanMergeRuns() throws IOException {
    while(true) {
      boolean changed = false;

      for (Map.Entry<Integer, List<Integer>> kv : runsByLevel.entrySet()) {
        int level = kv.getKey();
        List<Integer> runs = kv.getValue();
        if (runs.size() >= mergeFactor) {
          runsByLevel.remove(level);

          doAsync(new AsyncRunMerger(runs, level + 1));
          changed = true;
          break;
        }
      }
      if(!changed) break;
    }
  }

  public abstract void mergeRuns(List<Integer> inputs, int output) throws IOException;

  public class AsyncRunMerger implements Runnable {
    private final List<Integer> runs;
    private final int outputLevel;

    public AsyncRunMerger(List<Integer> runs, int outputLevel) {
      this.runs = runs;
      this.outputLevel = outputLevel;
    }

    @Override
    public void run() {
      try {
        int newId = allocate();
        mergeRuns(runs, newId);
        addNewRun(newId, outputLevel);
      } catch (Throwable e) {
        e.printStackTrace(System.err);
        throw new RuntimeException(e);
      }
    }
  }
}
