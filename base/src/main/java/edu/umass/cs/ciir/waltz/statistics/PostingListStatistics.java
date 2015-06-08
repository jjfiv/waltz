package edu.umass.cs.ciir.waltz.statistics;

import javax.annotation.Nonnull;

/**
 * @author jfoley
 */
public interface PostingListStatistics<Posting> {
  void add(Posting values);
  void add(PostingListStatistics<Posting> stat);
  @Nonnull
  PostingListStatistics<Posting> copy();
}
