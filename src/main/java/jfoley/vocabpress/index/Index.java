package jfoley.vocabpress.index;

import jfoley.vocabpress.dociter.movement.PostingMover;
import jfoley.vocabpress.feature.Feature;
import jfoley.vocabpress.postings.positions.PositionsList;

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
