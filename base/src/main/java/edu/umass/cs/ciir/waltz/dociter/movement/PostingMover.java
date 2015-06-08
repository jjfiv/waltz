package edu.umass.cs.ciir.waltz.dociter.movement;

import ciir.jfoley.chai.fn.SinkFn;
import edu.umass.cs.ciir.waltz.feature.MoverFeature;

/**
 * @author jfoley
 */
public interface PostingMover<X> extends Mover {
  /** always call matches(doc id) before calling this to make sure you're reading what you think you're reading! */
  X getCurrentPosting();

  /**
   * Turn this mover into a feature!
   * @return this as a feature.
   */
  default MoverFeature<X> getFeature() {
    return new MoverFeature<>(this);
  }

  default void collectValues(SinkFn<X> collector) {
    MoverFeature<X> feature = getFeature();
    execute(input -> collector.process(feature.getFeature(input)));
  }
}
