package jfoley.vocabpress.postings.impl;

import jfoley.vocabpress.postings.APosting;
import jfoley.vocabpress.postings.ScorePosting;

/**
 * @author jfoley
 */
public class SimpleScorePosting extends APosting implements ScorePosting {
  private final double score;

  protected SimpleScorePosting(int document, double score) {
    super(document);
    this.score = score;
  }

  @Override
  public double getScore() {
    return score;
  }
}
