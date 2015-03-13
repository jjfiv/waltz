package jfoley.vocabpress.example;

import ciir.jfoley.chai.collections.list.IntList;
import jfoley.vocabpress.dociter.movement.PostingMover;
import jfoley.vocabpress.index.Index;

/**
 * @author jfoley
 */
public class CountComparisonQuery {
  // Find me documents where Term A occurs more than Term B
  public static IntList greaterThan(Index index, String termA, String termB) {
    PostingMover<Integer> iterA = index.getCountsMover(termA);
    PostingMover<Integer> iterB = index.getCountsMover(termB);

    IntList output = new IntList();
    for(; !iterA.isDone(); iterA.next()) {
      int docId = iterA.currentKey();
      assert(iterA.matches(docId));
      iterB.moveTo(docId);

      int countB = 0;
      if(iterB.matches(docId)) {
        countB = iterB.getCurrentPosting();
      }
      int countA = iterA.getCurrentPosting();

      if(countA > countB) {
        output.add(docId);
      }
    }
    return output;
  }

}
