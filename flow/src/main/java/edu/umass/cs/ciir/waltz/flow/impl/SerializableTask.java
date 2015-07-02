package edu.umass.cs.ciir.waltz.flow.impl;

import ciir.jfoley.chai.lang.LazyPtr;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowTask;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowTaskState;

import java.io.Serializable;

/**
 * @author jfoley
 */
public abstract class SerializableTask<T extends Serializable, Input, Output> extends FlowTask<Input, Output> {
  private SerializableTaskState<T> state;

  public SerializableTask(T item) {
    this.state = new SerializableTaskState<>(item);
  }

  @Override
  public FlowTaskState getState() {
    return state;
  }

  @SuppressWarnings("unchecked")
  public
  LazyPtr<T> item = new LazyPtr<>(this.state::get);
}
