package edu.umass.cs.ciir.waltz.sys.counts;

import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.sys.KeyMetadata;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class CountMetadata implements KeyMetadata<Integer> {
  public int totalDocs = 0;
  public int maxCount = 0;
  public int totalCount = 0;

  @Override
  public int totalDocuments() {
    return totalDocs;
  }

  @Override
  public void accumulate(KeyMetadata o) {
    assert(o instanceof CountMetadata);
    CountMetadata rhs = (CountMetadata) o;
    this.totalDocs += rhs.totalDocs;
    this.totalCount += rhs.totalCount;
    this.maxCount = Math.max(this.maxCount, rhs.maxCount);
  }

  @Override
  public void accumulate(int document, Integer item) {
    totalDocs++;
    totalCount += item;
    maxCount = Math.max(maxCount, item);
  }

  @Override
  public KeyMetadata<Integer> zero() {
    return new CountMetadata();
  }

  @Override
  public DataChunk encode() {
    ByteBuilder bb = new ByteBuilder();
    bb.add(FixedSize.ints, totalDocs);
    bb.add(FixedSize.ints, maxCount);
    bb.add(FixedSize.ints, totalCount);
    return bb;
  }

  @Override
  public KeyMetadata<Integer> decode(InputStream input) throws IOException {
    CountMetadata m = new CountMetadata();
    m.totalDocs = FixedSize.ints.readImpl(input);
    m.maxCount = FixedSize.ints.readImpl(input);
    m.totalCount = FixedSize.ints.readImpl(input);
    return m;
  }


}
