package edu.umass.cs.ciir.waltz.coders.map.impl.vocab;

import ciir.jfoley.chai.math.StreamingStats;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author jfoley
 */
public class ListIndexTest {

  @Test
  public void testSimple() {
    HashMap<Integer,Integer> sizes = new HashMap<>();
    HashMap<Integer,Long> offsets = new HashMap<>();

    int N = 50000;
    Random rand = new Random(13);
    List<VocabEntry<Integer>> data = new ArrayList<>(N);
    for (int i = 0; i < N; i++) {
      long offset = rand.nextLong();
      int size = rand.nextInt();
      sizes.put(i, size);
      offsets.put(i, offset);
      data.add(new VocabEntry<>(i, offset, size));
    }

    ListIndex<VocabEntry<Integer>, Integer> index = ListIndex.create("none", data, VocabEntry::getKey);

    VocabEntry<Integer> e = index.find(33);
    assertEquals(33, e.key.intValue());
    assertEquals(sizes.get(33).intValue(), e.size);
    assertEquals(offsets.get(33).longValue(), e.offset);

    ListIndex<VocabEntry<Integer>, Integer> hashed = ListIndex.create("hashall", data, VocabEntry::getKey);

    System.err.println("HashAll: "+computeTiming(sizes, offsets, N, hashed));
    System.err.println("TwoLevel: "+computeTiming(sizes, offsets, N, ListIndex.create("TwoLevel", data, VocabEntry::getKey)));
    System.err.println("None: "+computeTiming(sizes, offsets, N, index));

  }

  private static StreamingStats computeTiming(HashMap<Integer, Integer> sizes, HashMap<Integer, Long> offsets, int n, ListIndex<VocabEntry<Integer>, Integer> index) {
    long startTime;
    long endTime;
    StreamingStats lookupTime = new StreamingStats();
    for (int i = 0; i < n; i++) {
      startTime = System.nanoTime();
      VocabEntry<Integer> found = index.find(i);
      endTime = System.nanoTime();
      assertNotNull(found);
      lookupTime.push((endTime - startTime) / 1e9);
      assertEquals(i, found.key.intValue());
      assertEquals(sizes.get(i).intValue(), found.size);
      assertEquals(offsets.get(i).longValue(), found.offset);
    }
    for (int i = n; i < n+400; i++) {
      startTime = System.nanoTime();
      VocabEntry<Integer> found = index.find(i);
      endTime = System.nanoTime();
      assertNull(found);
      lookupTime.push((endTime - startTime) / 1e9);
    }
    return lookupTime;
  }
}