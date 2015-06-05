package edu.umass.cs.ciir.waltz.index;

import edu.umass.cs.ciir.waltz.dociter.movement.PostingMover;
import edu.umass.cs.ciir.waltz.feature.Feature;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.List;

/**
 * @author jfoley
 */
public interface Index extends Closeable {
  // TODO longs here?
  int getCollectionLength();

  int getDocumentCount();

  @Nonnull
  List<Integer> getAllDocumentIds();

  @Nullable
  PostingMover<Integer> getCountsMover(String term);

  @Nullable
  PostingMover<PositionsList> getPositionsMover(String term);

  @Nullable
  Feature<Integer> getCounts(String term);

  @Nullable
  Feature<PositionsList> getPositions(String term);

  @Nullable
  String getDocumentName(int id);

  /**
   * Returns -1 when not found.
   * @param documentName the document name to query.
   * @return the identifier or -1 if not found.
   */
  int getDocumentId(String documentName);

  @Nonnull
  Feature<Integer> getLengths();

}
