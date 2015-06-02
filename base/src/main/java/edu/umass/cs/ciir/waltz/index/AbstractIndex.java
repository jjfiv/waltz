package edu.umass.cs.ciir.waltz.index;

import edu.umass.cs.ciir.waltz.feature.Feature;
import edu.umass.cs.ciir.waltz.feature.MoverFeature;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;

/**
 * @author jfoley.
 */
public abstract class AbstractIndex implements Index {
  @Override
  public Feature<Integer> getCounts(String term) {
    return new MoverFeature<>(getCountsMover(term));
  }

  @Override
  public Feature<PositionsList> getPositions(String term) {
    return new MoverFeature<>(getPositionsMover(term));
  }


}
