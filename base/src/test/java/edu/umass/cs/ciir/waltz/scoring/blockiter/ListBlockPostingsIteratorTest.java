package edu.umass.cs.ciir.waltz.scoring.blockiter;

import edu.umass.cs.ciir.waltz.dociter.IKeyBlock;
import edu.umass.cs.ciir.waltz.dociter.ListBlockPostingsIterator;
import edu.umass.cs.ciir.waltz.dociter.ValueBlock;
import edu.umass.cs.ciir.waltz.postings.Posting;
import edu.umass.cs.ciir.waltz.postings.SimplePosting;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ListBlockPostingsIteratorTest {

  @Test
  public void testNextKeyBlock() throws Exception {
    List<Posting<Integer>> exampleData = new ArrayList<>();
    for (int i = 0; i < 35; i++) {
      exampleData.add(SimplePosting.Count(i, i * 2));
    }

    ListBlockPostingsIterator<Integer> countBlocks = new ListBlockPostingsIterator<>(exampleData, 16);

    IKeyBlock keys = countBlocks.nextKeyBlock();
    Assert.assertEquals(0, keys.minKey());
    Assert.assertEquals(15, keys.maxKey());

    keys = countBlocks.nextKeyBlock();
    Assert.assertEquals(16, keys.minKey());
    Assert.assertEquals(31, keys.maxKey());

    ValueBlock<Integer> vals = countBlocks.nextValueBlock();
    Assert.assertEquals(17, keys.getKey(1));
    Assert.assertEquals(17 * 2, vals.getValue(1).intValue());

    keys = countBlocks.nextKeyBlock();
    Assert.assertEquals(32, keys.minKey());
    Assert.assertEquals(34, keys.maxKey());
  }
}