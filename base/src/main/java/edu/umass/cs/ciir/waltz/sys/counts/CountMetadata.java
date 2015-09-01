package edu.umass.cs.ciir.waltz.sys.counts;

import edu.umass.cs.ciir.waltz.coders.data.ByteBuilder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.sys.KeyMetadata;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class CountMetadata implements KeyMetadata<Integer> {
  public int totalDocs = 0;
  public int maxDoc = 0;
  public int maxCount = 0;
  public int totalCount = 0;

  @Override public int totalDocuments() { return totalDocs; }
  @Override public int maxDocument() { return maxDoc; }

  @Override
  public void accumulate(KeyMetadata o) {
    assert(o instanceof CountMetadata);
    CountMetadata rhs = (CountMetadata) o;
    this.totalDocs += rhs.totalDocs;
    this.maxDoc = Math.max(this.maxDoc, rhs.maxDoc);
    this.totalCount += rhs.totalCount;
    this.maxCount = Math.max(this.maxCount, rhs.maxCount);
  }

  @Override
  public void accumulate(int document, Integer item) {
    totalDocs++;
    totalCount += item;
    maxCount = Math.max(maxCount, item);
    this.maxDoc = Math.max(this.maxDoc, document);
  }

  @Override
  public KeyMetadata<Integer> zero() {
    return new CountMetadata();
  }

  @Override
  public DataChunk encode() {
    ByteBuilder bb = new ByteBuilder();
    bb.add(VarUInt.instance, totalDocs);
    bb.add(VarUInt.instance, maxDoc);
    bb.add(VarUInt.instance, maxCount);
    bb.add(VarUInt.instance, totalCount);
    return bb;
  }

  @Override
  public KeyMetadata<Integer> decode(InputStream input) throws IOException {
    CountMetadata m = new CountMetadata();
    m.totalDocs = VarUInt.instance.readImpl(input);
    m.maxDoc = VarUInt.instance.readImpl(input);
    m.maxCount = VarUInt.instance.readImpl(input);
    m.totalCount = VarUInt.instance.readImpl(input);
    return m;
  }


}
