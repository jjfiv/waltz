package edu.umass.cs.ciir.waltz.sys.counts;

import edu.umass.cs.ciir.waltz.sys.KeyMetadata;

/**
 * @author jfoley
 */
public class CountMetadata implements KeyMetadata<Integer, CountMetadata> {
  public int totalDocs = 0;
  public int maxCount = 0;
  public int totalCount = 0;

  @Override
  public int totalDocuments() {
    return totalDocs;
  }

  @Override
  public void accumulate(CountMetadata o) {
    this.totalDocs += o.totalDocs;
    this.totalCount += o.totalCount;
    this.maxCount = Math.max(this.maxCount, o.maxCount);
  }

  @Override
  public void accumulate(int document, Integer item) {
    totalDocs++;
    totalCount += item;
    maxCount = Math.max(maxCount, item);
  }

  @Override
  public CountMetadata zero() {
    return new CountMetadata();
  }
}
