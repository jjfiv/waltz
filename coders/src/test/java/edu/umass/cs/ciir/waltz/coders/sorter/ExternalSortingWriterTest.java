package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.collections.IntRange;
import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

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

    try (TemporaryDirectory tmpdir = new TemporaryDirectory()) {
      try (ExternalSortingWriter<Integer> sorter = new ExternalSortingWriter<>(tmpdir.get(), VarUInt.instance, Comparing.defaultComparator(), maxItemsInMemory, mergeFactor)) {
        for (int input : shuffled) {
          sorter.process(input);
        }
      }
    }
    // No crashes!
  }
}