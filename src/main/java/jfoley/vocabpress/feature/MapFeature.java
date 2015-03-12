package jfoley.vocabpress.feature;

import jfoley.vocabpress.movement.Mover;
import jfoley.vocabpress.movement.IdSetMover;
import jfoley.vocabpress.postings.Posting;

import java.util.Map;

/**
 * @author jfoley
 */
public class MapFeature<T extends Posting> implements Feature<T> {
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

  @Override
  public Mover getMover() {
    return new IdSetMover(data.keySet());
  }
}
