package edu.umass.cs.ciir.waltz.flow.impl;

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

  public T getItem() {
    return this.state.get();
  }

  @Override
  public FlowTaskState getState() {
    return state;
  }

}
