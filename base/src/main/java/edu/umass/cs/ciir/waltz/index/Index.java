package edu.umass.cs.ciir.waltz.index;

import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.feature.Feature;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;

import java.io.Closeable;
import java.util.List;

/**
 * @author jfoley
 */
public interface Index extends Closeable {
  // TODO longs here?
  int getCollectionLength();

  int getDocumentCount();

  List<Integer> getAllDocumentIds();

  PostingMover<Integer> getCountsMover(String term);

  PostingMover<PositionsList> getPositionsMover(String term);

  Feature<Integer> getCounts(String term);

  Feature<PositionsList> getPositions(String term);

  String getDocumentName(int id);

  Feature<Integer> getLengths();
}
