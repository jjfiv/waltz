package edu.umass.cs.ciir.waltz.feature;

/**
 * This interface could be wrapped around Map&lt;int, X&gt;
 * @author jfoley
 */
public interface Feature<X> {
  /** This determines if there is a feature at a specific value. */
  public boolean hasFeature(int key);
  /**
   * This is usually called after a check to hasFeature, but may return null if !hasFeature().
   * If you're using null semantically, hasFeature will help with that.
   */
  public X getFeature(int key);
}
