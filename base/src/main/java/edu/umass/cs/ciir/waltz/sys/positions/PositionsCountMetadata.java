package edu.umass.cs.ciir.waltz.sys.positions;

import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.sys.KeyMetadata;

/**
 * @author jfoley
 */
public class PositionsCountMetadata implements KeyMetadata<PositionsList, PositionsCountMetadata> {

  public int totalDocs = 0;
  public int maxCount = 0;
  public int totalCount = 0;
  public int highestPosition = 0;

  @Override
  public int totalDocuments() {
    return totalDocs;
  }

  @Override
  public void accumulate(PositionsCountMetadata m) {
    totalDocs += m.totalDocs;
    totalCount += m.totalCount;
    maxCount = Math.max(maxCount, m.maxCount);
    highestPosition = Math.max(highestPosition, m.highestPosition);
  }

  @Override
  public void accumulate(int document, PositionsList item) {
    totalDocs++;
    int count = item.size();
    totalCount += count;
    maxCount = Math.max(count, maxCount);
    highestPosition = Math.max(highestPosition, item.getPosition(count - 1));
  }

  @Override
  public PositionsCountMetadata zero() {
    return new PositionsCountMetadata();
  }
}
