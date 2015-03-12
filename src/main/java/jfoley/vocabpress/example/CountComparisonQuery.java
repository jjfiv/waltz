package jfoley.vocabpress.example;

import ciir.jfoley.chai.collections.list.IntList;
import jfoley.vocabpress.dociter.movement.PostingMover;
import jfoley.vocabpress.index.Index;
import jfoley.vocabpress.postings.CountPosting;

/**
 * @author jfoley
 */
public class CountComparisonQuery {
  // Find me documents where Term A occurs more than Term B
  public static IntList greaterThan(Index index, String termA, String termB) {
    PostingMover<CountPosting> iterA = index.getCountsMover(termA);
    PostingMover<CountPosting> iterB = index.getCountsMover(termB);

    IntList output = new IntList();
    for(; !iterA.isDone(); iterA.next()) {
      int docId = iterA.currentKey();
      assert(iterA.matches(docId));
      iterB.moveTo(docId);

      int countB = 0;
      if(iterB.matches(docId)) {
        countB = iterB.getCurrentPosting().getCount();
      }
      int countA = iterA.getCurrentPosting().getCount();

      if(countA > countB) {
        output.add(docId);
      }
    }
    return output;
  }

}
