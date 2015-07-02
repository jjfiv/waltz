package edu.umass.cs.ciir.waltz.flow.impl;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowSink;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowSource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author jfoley
 */
public class FlowCollectionSource<T extends Serializable> extends FlowSource<T> implements SerializableStateful<ArrayList<T>> {
  private ArrayList<T> items;

  public FlowCollectionSource() { }
  public FlowCollectionSource(Collection<? extends T> input) {
    this.items = new ArrayList<>(input);
  }

  @Override
  public void run(FlowSink<T> output) throws Exception {
    assert(items != null);
    output.process(items);
  }

  @Override
  public ArrayList<T> getState() {
    return items;
  }

  @Override
  public void setState(ArrayList<T> object) {
    this.items = object;
  }
}
