package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.collections.IntRange;
import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.collections.util.QuickSort;
import ciir.jfoley.chai.io.IO;
import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author jfoley.
 */
public class ExternalSortingWriterTest {

  @Test
  public void testProcess() throws Exception {
    List<Integer> data = IntRange.exclusive(0,10000);
    int maxItemsInMemory = 100;
    int mergeFactor = 5;

    List<Integer> shuffled = new IntList(data);
    Collections.shuffle(shuffled);

    List<Integer> hackedMerging = new IntList();
    List<List<Integer>> sortedRuns = new ArrayList<>();

    try (TemporaryDirectory tmpdir = new TemporaryDirectory()) {
      try (ExternalSortingWriter<Integer> sorter = new ExternalSortingWriter<>(tmpdir.get(), VarUInt.instance, Comparing.defaultComparator(), maxItemsInMemory, mergeFactor)) {
        for (int input : shuffled) {
          sorter.process(input);
        }

        List<RunReader<Integer>> readers = new ArrayList<>();
        for (List<Integer> ids : sorter.runsByLevel.values()) {
          for (Integer id : ids) {
            readers.add(new RunReader<>(sorter.cmp, sorter.countCoder, sorter.objCoder, IO.openInputStream(sorter.nameForId(id))));
          }
        }
        for (RunReader<Integer> reader : readers) {
          List<Integer> run = new IntList();
          while(reader.hasNext()) {
            run.add(reader.next());
          }
          sortedRuns.add(run);
          hackedMerging.addAll(run);
        }
      }
    }

    // The parameters here have been chosen to exercise as much code as possible.
    // Look, ma, no data loss.
    assertEquals(shuffled.size(), hackedMerging.size());
    QuickSort.sort(hackedMerging);
    assertEquals(data, hackedMerging);

    // Prove by example that the merging of runs kind of worked.
    for (List<Integer> sortedRun : sortedRuns) {
      for (int i = 0; i < sortedRun.size()-1; i++) {
        assertTrue(sortedRun.get(i) < sortedRun.get(i+1));
      }
    }
  }

}