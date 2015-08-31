package edu.umass.cs.ciir.waltz.sys.counts;

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
public class CountMetadataCoder extends Coder<CountMetadata> {
  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(CountMetadata m) throws IOException {
    ByteBuilder bb = new ByteBuilder();
    bb.add(FixedSize.ints, m.totalDocs);
    bb.add(FixedSize.ints, m.maxCount);
    bb.add(FixedSize.ints, m.totalCount);
    return bb;
  }

  @Nonnull
  @Override
  public CountMetadata readImpl(InputStream inputStream) throws IOException {
    CountMetadata m = new CountMetadata();
    m.totalDocs = FixedSize.ints.readImpl(inputStream);
    m.maxCount = FixedSize.ints.readImpl(inputStream);
    m.totalCount = FixedSize.ints.readImpl(inputStream);
    return m;
  }
}
