package edu.umass.cs.ciir.waltz.flow.lang;

import edu.umass.cs.ciir.waltz.flow.impl.SerializableTask;
import edu.umass.cs.ciir.waltz.flow.impl.SerializableTaskState;
import edu.umass.cs.ciir.waltz.flow.lambda.FMapFn;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowSink;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowTask;

import javax.annotation.Nonnull;
import java.io.Serializable;

/** Chainable, stream-like operations for SourceNode and TaskNode */
public interface FlowOpNode<T> extends FlowNode {
  default <X> FlowOpNode<X> connect(@Nonnull String name, @Nonnull FlowTask<T, X> task) {
    FlowTaskNode<X> next = new FlowTaskNode<>(name, task);
    FlowNode.link(this, next);
    return next;
  }
  default <X> FlowOpNode<X> map(@Nonnull String name, @Nonnull FMapFn<T, X> mapper) {
    FlowTask<T,X> m2Task = new SerializableTaskFn<>();
    m2Task.setState(new SerializableTaskState<FTaskFn<T,X>>((input, output) -> {
      try {
        output.process(mapper.map(input));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }));

    FlowTaskNode<X> next = new FlowTaskNode<>(name, m2Task);
    FlowNode.link(this, next);
    return next;
  }
  default FlowOpNode<T> collect(String name, FlowSink<T> sink) {
    FlowNode.link(this, new FlowSinkNode<>(name, sink));
    return this;
  }

  interface FTaskFn<Input,Output> extends Serializable {
    void run(Input input, FlowSink<Output> output);
  }
  class SerializableTaskFn<Input,Output> extends SerializableTask<FTaskFn<Input,Output>, Input,Output> {
    @Override
    protected void run(Input input, FlowSink<Output> output) throws Exception {
      getItem().run(input, output);
    }
  }
}
