package edu.umass.cs.ciir.waltz.statistics;

import ciir.jfoley.chai.fn.GenerateFn;

import javax.annotation.Nonnull;

/**
 * @author jfoley
 */
public class DefaultPostingListStatistics<T> implements PostingListStatistics<T>, GenerateFn<Integer> {
  public int documentFrequency;

  public DefaultPostingListStatistics() { this(0); }
  public DefaultPostingListStatistics(int documentFrequency) {
    this.documentFrequency = documentFrequency;
  }

  @Override
  public void add(T ignored) {
    documentFrequency++;
  }

  @Override
  public void add(PostingListStatistics<T> stat) {
    if(stat instanceof DefaultPostingListStatistics) {
      documentFrequency += ((DefaultPostingListStatistics) stat).documentFrequency;
    }
  }

  @Override
  @Nonnull
  public PostingListStatistics<T> copy() {
    return new DefaultPostingListStatistics<>(documentFrequency);
  }

  @Override
  public Integer get() {
    return documentFrequency;
  }
}
