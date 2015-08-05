package edu.umass.cs.ciir.waltz.galago.io;

import ciir.jfoley.chai.collections.chained.ChaiIterable;
import ciir.jfoley.chai.io.TemporaryFile;
import ciir.jfoley.chai.random.Sample;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.map.IOMap;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriterRawWrapper;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.index.mem.MemoryPositionsIndex;
import edu.umass.cs.ciir.waltz.io.postings.PositionsListCoder;
import edu.umass.cs.ciir.waltz.io.postings.format.BlockedPostingsCoder;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import org.junit.Assert;
import org.junit.Test;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.*;

public class SimplePostingListFormatTest {
  @Test
  public void integrationTest() throws IOException {

    MemoryPositionsIndex memIndex = new MemoryPositionsIndex();
    memIndex.addDocument("hello", Arrays.asList("the", "dog", "runs"));
    memIndex.addDocument("shakespeare", Arrays.asList("to", "be", "or", "not", "to", "be"));

    Random rand = new Random();
    for (int i = 0; i < 1000; i++) {
      memIndex.addDocument("doc"+i, Sample.strings(rand, rand.nextInt(40)+10));
    }

    Parameters argp = Parameters.create();
    argp.put("valCoder", BlockedPostingsCoder.class.getName());
    argp.put("innerValCoder", PositionsListCoder.class.getName());
    argp.put("keyCoder", FixedSize.ints.getClass().getName());

    try (TemporaryFile tmpFile = new TemporaryFile("positions", "index")) {
      try (IOMapWriterRawWrapper<Integer, PostingMover<PositionsList>> postingsWriter =
               GalagoIO.getIOMapWriter(
                   FixedSize.ints,
                   new BlockedPostingsCoder<>(new PositionsListCoder()),
                   tmpFile.getPath(),
                   argp
               )) {
        assertNotNull(postingsWriter);

        // get & sort by values.
        List<Map.Entry<String, Integer>> items = ChaiIterable
            .create(memIndex.terms.getAllItems())
            .sorted((a,b) -> Integer.compare(a.getValue(), b.getValue()))
            .intoList();

        for (Map.Entry<String, Integer> kv : items) {
          postingsWriter.put(kv.getValue(), memIndex.getPositionsMover(kv.getKey()));
        }
      } // close disk writer

      try (IOMap<Integer, PostingMover<PositionsList>> part =
               GalagoIO.openIOMap(
                   FixedSize.ints,
                   new BlockedPostingsCoder<>(new PositionsListCoder()),
                   tmpFile.getPath())) {

        // get & sort by values.
        List<Map.Entry<String, Integer>> items = ChaiIterable
            .create(memIndex.terms.getAllItems())
            .sorted((a,b) -> Integer.compare(a.getValue(), b.getValue()))
            .intoList();

        //part.get()
        for (Map.Entry<String, Integer> kv : items) {
          PostingMover<PositionsList> fromMemIndex = memIndex.getPositionsMover(kv.getKey());
          PostingMover<PositionsList> fromDisk = part.get(kv.getValue());

          int lastId = -1;
          assertFalse(fromMemIndex.isDone());
          assertFalse(fromDisk.isDone());
          for(fromMemIndex.start(), fromDisk.start() ; !fromMemIndex.isDone(); fromDisk.next(), fromMemIndex.next()) {
            assertTrue(lastId < fromMemIndex.currentKey());
            assertTrue(lastId < fromDisk.currentKey());
            lastId = fromMemIndex.currentKey();

            Assert.assertEquals(fromDisk.currentKey(), fromMemIndex.currentKey());
            Assert.assertEquals(fromDisk.getCurrentPosting(), fromMemIndex.getCurrentPosting());
          }
          assertTrue(fromMemIndex.isDone());
          assertTrue(fromDisk.isDone());
        }
      } // close disk reader

    } // delete temp file



  }

}