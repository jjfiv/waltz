package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.collections.IntRange;
import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.collections.util.*;
import ciir.jfoley.chai.io.TemporaryDirectory;
import ciir.jfoley.chai.random.Sample;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.CharsetCoders;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.reduce.Reducer;
import edu.umass.cs.ciir.waltz.coders.tuple.DiskMapAtom;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

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
      try (ExternalSortingWriter<Integer> sorter = new ExternalSortingWriter<>(tmpdir.get(), VarUInt.instance, new Reducer.NullReducer<>(), Comparing.defaultComparator(), maxItemsInMemory, mergeFactor)) {
        for (int input : shuffled) {
          sorter.process(input);
        }
        sorter.flush();

        List<RunReader<Integer>> readers = new ArrayList<>();
        for (List<Integer> ids : sorter.runsByLevel.values()) {
          for (Integer id : ids) {
            readers.add(new RunReader<>(sorter.cmp, sorter.objCoder, sorter.nameForId(id)));
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


  @Test
  public void testStrings() throws IOException {
    List<String> rawData = Sample.strings(new Random(), 1000);
    List<String> sortedRaw = new ArrayList<>(rawData);
    Collections.sort(sortedRaw);

    try (TemporaryDirectory tmpdir = new TemporaryDirectory()) {
      try (ExternalSortingWriter<String> sorter = new ExternalSortingWriter<>(tmpdir.get(), CharsetCoders.utf8)) {
        rawData.forEach(sorter::process);
        sorter.close();

        // Test collection wrapper of sorter.getOutput()
        List<String> sortified = IterableFns.intoList(sorter.getOutput());
        assertEquals(sortedRaw, sortified);
      }
    }
  }

  @Test
  public void testWordCountReduce() throws IOException {
    Random rand = new Random();
    List<DiskMapAtom<String,Integer>> testData = new ArrayList<>();
    List<String> words = Sample.strings(rand, 1000);
    List<Integer> counts = Sample.randomIntegers(1000, 255);
    Coder<DiskMapAtom<String,Integer>> coder = new DiskMapAtom.DiskMapAtomCoder<>(CharsetCoders.utf8, VarUInt.instance);
    for (Pair<String, Integer> kv : ListFns.zip(words, counts)) {
      DiskMapAtom<String,Integer> wc = new DiskMapAtom<>(kv.getKey(), Math.abs(kv.getValue()));
      testData.add(wc);
      DiskMapAtom<String,Integer> recoded = coder.read(coder.write(wc));
      assertEquals(wc, recoded);

    }

    Map<String,Integer> frequencies = new HashMap<>(testData.size());
    for (DiskMapAtom<String,Integer> wordCount : testData) {
      MapFns.addOrIncrement(frequencies, wordCount.left, wordCount.right);
    }

    // Make sure we can sort this type:
    List<DiskMapAtom<String,Integer>> sortedExternally;
    try (TemporaryDirectory tmpdir = new TemporaryDirectory()) {
      try (ExternalSortingWriter<DiskMapAtom<String,Integer>> sorter = new ExternalSortingWriter<>(
          tmpdir.get(),
          coder,
          null, // no reducer
          Comparing.defaultComparator(),
          25, // make sure some flushes actually happen in this test
          2 // make sure merge factor is weird
      )) {
        testData.forEach(sorter::process);

        sorter.flush();
        sortedExternally = IterableFns.intoList(sorter.getOutput());
      }
    }
    List<DiskMapAtom<String,Integer>> sortedInternally = new ArrayList<>(testData);
    Collections.sort(sortedInternally);
    assertEquals(sortedInternally, sortedExternally);

    try (TemporaryDirectory tmpdir = new TemporaryDirectory()) {
      try (ExternalSortingWriter<DiskMapAtom<String,Integer>> sorter = new ExternalSortingWriter<>(
          tmpdir.get(),
          coder,
          new DiskMapAtom.DiskMapAtomReducer<>((x,y) -> x+y),
          Comparing.defaultComparator(),
          25, // make sure some flushes actually happen in this test
          2 // make sure merge factor is weird
      )) {
        testData.forEach(sorter::process);

        sorter.flush();

        for (DiskMapAtom<String,Integer> wordCount : sorter.getOutput()) {
          assertEquals(frequencies.get(wordCount.left), wordCount.right);
        }
      }
    }

  }
}