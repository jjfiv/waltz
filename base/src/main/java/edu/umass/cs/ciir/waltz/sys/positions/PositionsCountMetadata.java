package edu.umass.cs.ciir.waltz.sys.positions;

import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.sys.KeyMetadata;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class PositionsCountMetadata implements KeyMetadata<PositionsList> {

  public int totalDocs = 0;
  public int maxDoc = 0;
  public int maxCount = 0;
  public int totalCount = 0;
  public int highestPosition = 0;

  @Override public int totalDocuments() { return totalDocs; }
  @Override public int maxDocument() { return maxDoc; }

  @Override
  public void accumulate(KeyMetadata o) {
    assert(o instanceof PositionsCountMetadata);
    PositionsCountMetadata m = (PositionsCountMetadata) o;
    totalDocs += m.totalDocs;
    maxDoc = Math.max(maxDoc, m.maxDoc);
    totalCount += m.totalCount;
    maxCount = Math.max(maxCount, m.maxCount);
    highestPosition = Math.max(highestPosition, m.highestPosition);
  }

  @Override
  public void accumulate(int document, PositionsList item) {
    totalDocs++;
    int count = item.size();
    totalCount += count;
    maxDoc = Math.max(maxDoc, document);
    maxCount = Math.max(count, maxCount);
    highestPosition = Math.max(highestPosition, item.getPosition(count - 1));
  }

  @Override
  public String toString() {
    return "{totalDocs: "+totalDocs+" totalCount:"+totalCount+" maxDoc:"+maxDoc+" maxCount:"+maxCount+"}";
  }

  @Override
  public KeyMetadata<PositionsList> zero() {
    return new PositionsCountMetadata();
  }

  @Override
  public DataChunk encode() {
    ByteBuilder bb = new ByteBuilder();
    bb.add(VarUInt.instance, totalDocs);
    bb.add(VarUInt.instance, maxDoc);
    bb.add(VarUInt.instance, maxCount);
    bb.add(VarUInt.instance, totalCount);
    bb.add(VarUInt.instance, highestPosition);
    return bb;
  }

  @Override
  public KeyMetadata<PositionsList> decode(InputStream input) throws IOException {
    PositionsCountMetadata m = new PositionsCountMetadata();
    m.totalDocs = VarUInt.instance.readImpl(input);
    m.maxDoc = VarUInt.instance.readImpl(input);
    m.maxCount = VarUInt.instance.readImpl(input);
    m.totalCount = VarUInt.instance.readImpl(input);
    m.highestPosition = VarUInt.instance.readImpl(input);
    return m;
  }
}
