package jfoley.vocabpress.scoring.blockiter;

import jfoley.vocabpress.scoring.CountPosting;
import jfoley.vocabpress.scoring.impl.SimpleCountPosting;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ListBlockPostingsIteratorTest {

  @Test
  public void testNextKeyBlock() throws Exception {
    List<CountPosting> exampleData = new ArrayList<>();
    for (int i = 0; i < 35; i++) {
      exampleData.add(new SimpleCountPosting(i, i*2));
    }

    ListBlockPostingsIterator<CountPosting> countBlocks = new ListBlockPostingsIterator<>(exampleData, 16);

    KeyBlock keys = countBlocks.nextKeyBlock();
    assertEquals(0, keys.min());
    assertEquals(15, keys.max());

    keys = countBlocks.nextKeyBlock();
    assertEquals(16, keys.min());
    assertEquals(31, keys.max());

    ValueBlock<CountPosting> vals = countBlocks.nextValueBlock();
    assertEquals(17, vals.get(1).getKey());
    assertEquals(17*2, vals.get(1).getCount());

    keys = countBlocks.nextKeyBlock();
    assertEquals(32, keys.min());
    assertEquals(34, keys.max());
  }
}