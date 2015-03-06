package jfoley.vocabpress.scoring.blockiter.movement;

import jfoley.vocabpress.scoring.Posting;

/**
 * @author jfoley
 */
public interface Feature<X extends Posting> {
  public boolean hasFeature(int id);
  public X getFeature(int id);
}
