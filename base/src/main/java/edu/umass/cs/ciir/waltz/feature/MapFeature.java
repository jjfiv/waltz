package edu.umass.cs.ciir.waltz.feature;

import edu.umass.cs.ciir.waltz.dociter.movement.IdSetMover;
import edu.umass.cs.ciir.waltz.dociter.movement.Mover;

import java.util.Map;

/**
 * @author jfoley
 */
public class MapFeature<T> implements Feature<T> {
  private final Map<Integer, T> data;
  public MapFeature(Map<Integer, T> data) {
    this.data = data;
  }

  @Override
  public boolean hasFeature(int key) {
    return data.get(key) != null;
  }

  @Override
  public T getFeature(int key) {
    return data.get(key);
  }

  public Mover getAsMover() {
    return new IdSetMover(data.keySet());
  }
}
