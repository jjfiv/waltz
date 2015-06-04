package edu.umass.cs.ciir.waltz.feature;

import javax.annotation.Nullable;

/**
 * This interface could be wrapped around Map&lt;int, X&gt;
 * @author jfoley
 */
public interface Feature<X> {
  /** This determines if there is a feature at a specific value. */
  boolean hasFeature(int key);
  /**
   * This is usually called after a check to hasFeature, but may return null if !hasFeature().
   * If you're using null semantically, hasFeature will help with that.
   */
  @Nullable
  X getFeature(int key);
}
