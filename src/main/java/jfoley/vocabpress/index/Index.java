package jfoley.vocabpress.index;

import jfoley.vocabpress.dociter.movement.PostingMover;
import jfoley.vocabpress.feature.Feature;
import jfoley.vocabpress.postings.CountPosting;
import jfoley.vocabpress.postings.positions.PositionsPosting;

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

  PostingMover<CountPosting> getCountsMover(String term);

  PostingMover<PositionsPosting> getPositionsMover(String term);

  Feature<CountPosting> getCounts(String term);

  Feature<PositionsPosting> getPositions(String term);

  String getDocumentName(int id);

  Feature<Integer> getLengths();
}
