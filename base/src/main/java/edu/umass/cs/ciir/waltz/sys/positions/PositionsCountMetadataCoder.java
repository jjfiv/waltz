package edu.umass.cs.ciir.waltz.sys.positions;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class PositionsCountMetadataCoder extends Coder<PositionsCountMetadata> {
  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(PositionsCountMetadata m) throws IOException {
    ByteBuilder bb = new ByteBuilder();
    bb.add(FixedSize.ints, m.totalDocs);
    bb.add(FixedSize.ints, m.maxCount);
    bb.add(FixedSize.ints, m.totalCount);
    bb.add(FixedSize.ints, m.highestPosition);
    return bb;
  }

  @Nonnull
  @Override
  public PositionsCountMetadata readImpl(InputStream inputStream) throws IOException {
    PositionsCountMetadata m = new PositionsCountMetadata();
    m.totalDocs = FixedSize.ints.readImpl(inputStream);
    m.maxCount = FixedSize.ints.readImpl(inputStream);
    m.totalCount = FixedSize.ints.readImpl(inputStream);
    m.highestPosition = FixedSize.ints.readImpl(inputStream);
    return m;
  }
}
