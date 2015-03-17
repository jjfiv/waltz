package edu.umass.cs.ciir.waltz.io.postings;

import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.chained.ChaiMap;
import ciir.jfoley.chai.io.TemporaryFile;
import ciir.jfoley.chai.random.Sample;
import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.index.mem.MemoryPositionsIndex;
import edu.umass.cs.ciir.waltz.io.IOMap;
import edu.umass.cs.ciir.waltz.io.coders.FixedSize;
import edu.umass.cs.ciir.waltz.io.galago.GalagoDiskMap;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import org.junit.Test;
import org.lemurproject.galago.utility.Parameters;

import java.io.IOException;
import java.util.Arrays;
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
    argp.put("valCoder", SimplePostingListFormat.PostingCoder.class.getName());
    argp.put("innerValCoder", PositionsListCoder.class.getName());
    argp.put("keyCoder", FixedSize.ints.getClass().getName());

    try (TemporaryFile tmpFile = new TemporaryFile("positions", "index")) {
      try (GalagoDiskMap.Writer<Integer, PostingMover<PositionsList>> postingsWriter =
               new GalagoDiskMap.Writer<>(
                   FixedSize.ints,
                   new SimplePostingListFormat.PostingCoder<>(new PositionsListCoder()),
                   tmpFile.getPath(),
                   argp
               )) {
        assertNotNull(postingsWriter);
        for (Pair<String, Integer> kv : ChaiMap.create(memIndex.terms.getAllItems()).pairs().sorted(Pair.cmpRight())) {
          postingsWriter.put(kv.getValue(), memIndex.getPositionsMover(kv.getKey()));
        }
      } // close disk writer

      try (IOMap<Integer, PostingMover<PositionsList>> part =
               new GalagoDiskMap<>(
                   FixedSize.ints,
                   new SimplePostingListFormat.PostingCoder<>(new PositionsListCoder()),
                   tmpFile.getPath())) {
        //part.get()
        for (Pair<String, Integer> kv : ChaiMap.create(memIndex.terms.getAllItems()).pairs().sorted(Pair.cmpRight())) {
          PostingMover<PositionsList> fromMemIndex = memIndex.getPositionsMover(kv.getKey());
          PostingMover<PositionsList> fromDisk = part.get(kv.getValue());

          int lastId = -1;
          assertFalse(fromMemIndex.isDone());
          assertFalse(fromDisk.isDone());
          for( ; !fromMemIndex.isDone(); fromDisk.next(), fromMemIndex.next()) {
            assertTrue(lastId < fromMemIndex.currentKey());
            assertTrue(lastId < fromDisk.currentKey());
            lastId = fromMemIndex.currentKey();

            assertEquals(fromDisk.currentKey(), fromMemIndex.currentKey());
            assertEquals(fromDisk.getCurrentPosting().toList(), fromMemIndex.getCurrentPosting().toList());
          }
          assertTrue(fromMemIndex.isDone());
          assertTrue(fromDisk.isDone());
        }
      } // close disk reader

    } // delete temp file



  }

}