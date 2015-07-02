package edu.umass.cs.ciir.waltz.flow.impl;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowTask;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author jfoley
 */
public abstract class SerializableTask<T extends Serializable, Input, Output> extends FlowTask<Input, Output> {
  public SerializableTask() {
    this.setState(new SerializableTaskState<>());
  }

  public T getItem() {
    return Objects.requireNonNull(this.getState()).get();
  }

  @SuppressWarnings("unchecked")
  @Override
  public SerializableTaskState<T> getState() {
    return (SerializableTaskState<T>) super.getState();
  }

}
