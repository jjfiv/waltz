package jfoley.vocabpress.scoring.blockiter;

import jfoley.vocabpress.postings.CountPosting;
import jfoley.vocabpress.postings.impl.SimpleCountPosting;
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

    IKeyBlock keys = countBlocks.nextKeyBlock();
    assertEquals(0, keys.minKey());
    assertEquals(15, keys.maxKey());

    keys = countBlocks.nextKeyBlock();
    assertEquals(16, keys.minKey());
    assertEquals(31, keys.maxKey());

    ValueBlock<CountPosting> vals = countBlocks.nextValueBlock();
    assertEquals(17, vals.getValue(1).getKey());
    assertEquals(17*2, vals.getValue(1).getCount());

    keys = countBlocks.nextKeyBlock();
    assertEquals(32, keys.minKey());
    assertEquals(34, keys.maxKey());
  }
}