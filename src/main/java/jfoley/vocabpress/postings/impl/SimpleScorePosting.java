package jfoley.vocabpress.postings.impl;

import jfoley.vocabpress.postings.APosting;

/**
 * @author jfoley
 */
public class SimpleScorePosting extends APosting<Double> {
  private final double score;

  protected SimpleScorePosting(int document, double score) {
    super(document);
    this.score = score;
  }

  public double getScore() {
    return score;
  }

  @Override
  public Double getValue() {
    return getScore();
  }
}
