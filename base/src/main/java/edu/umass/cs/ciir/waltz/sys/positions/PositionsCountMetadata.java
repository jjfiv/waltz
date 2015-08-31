package edu.umass.cs.ciir.waltz.sys.positions;

import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.sys.KeyMetadata;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class PositionsCountMetadata implements KeyMetadata<PositionsList> {

  public int totalDocs = 0;
  public int maxCount = 0;
  public int totalCount = 0;
  public int highestPosition = 0;

  @Override
  public int totalDocuments() {
    return totalDocs;
  }

  @Override
  public void accumulate(KeyMetadata o) {
    assert(o instanceof PositionsCountMetadata);
    PositionsCountMetadata m = (PositionsCountMetadata) o;
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
  public KeyMetadata<PositionsList> zero() {
    return new PositionsCountMetadata();
  }

  @Override
  public DataChunk encode() {
    ByteBuilder bb = new ByteBuilder();
    bb.add(FixedSize.ints, totalDocs);
    bb.add(FixedSize.ints, maxCount);
    bb.add(FixedSize.ints, totalCount);
    bb.add(FixedSize.ints, highestPosition);
    return bb;
  }

  @Override
  public KeyMetadata<PositionsList> decode(InputStream input) throws IOException {
    PositionsCountMetadata m = new PositionsCountMetadata();
    m.totalDocs = FixedSize.ints.readImpl(input);
    m.maxCount = FixedSize.ints.readImpl(input);
    m.totalCount = FixedSize.ints.readImpl(input);
    m.highestPosition = FixedSize.ints.readImpl(input);
    return m;
  }
}
