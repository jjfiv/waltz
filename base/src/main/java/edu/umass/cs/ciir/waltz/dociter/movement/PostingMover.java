package edu.umass.cs.ciir.waltz.dociter.movement;

import ciir.jfoley.chai.fn.SinkFn;
import edu.umass.cs.ciir.waltz.feature.MoverFeature;
import edu.umass.cs.ciir.waltz.sys.KeyMetadata;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jfoley
 */
public interface PostingMover<X> extends Mover {
  /** always call matches(doc id) before calling this to make sure you're reading what you think you're reading! */
  X getCurrentPosting();

  /**
   * The difference here is the generic type is applied.
   * {@inheritDoc}
   */
  @Override
  @Nullable
  KeyMetadata<X> getMetadata();

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

  default Map<Integer,X> toMap() {
    Map<Integer,X> data = new HashMap<>();
    MoverFeature<X> feature = getFeature();
    execute(input -> data.put(input, feature.getFeature(input)));
    return data;
  }
}
