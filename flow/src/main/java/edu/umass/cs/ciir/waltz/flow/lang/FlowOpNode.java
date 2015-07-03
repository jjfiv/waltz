package edu.umass.cs.ciir.waltz.flow.lang;

import edu.umass.cs.ciir.waltz.flow.impl.SerializableTaskFn;
import edu.umass.cs.ciir.waltz.flow.lambda.FMapFn;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowJob;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowSink;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowTask;

import javax.annotation.Nonnull;

/** Chainable, stream-like operations for SourceNode and TaskNode */
public interface FlowOpNode<T> extends FlowNode {
  default <X> FlowOpNode<X> connect(@Nonnull String name, @Nonnull FlowTask<T, X> task) {
    FlowTaskNode<X> next = new FlowTaskNode<>(name, task);
    FlowNode.link(this, next);
    return next;
  }
  default <X> FlowOpNode<X> map(@Nonnull String name, @Nonnull FMapFn<T, X> mapper) {
    SerializableTaskFn<T,X> m2Task = new SerializableTaskFn<T,X>();
    m2Task.setState((input, output) -> {
      try {
        output.process(mapper.map(input));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    FlowTaskNode<X> next = new FlowTaskNode<>(name, m2Task);
    FlowNode.link(this, next);
    return next;
  }
  default FlowOpNode<T> collect(String name, FlowSink<T> sink) {
    FlowNode.link(this, new FlowSinkNode<>(name, sink));
    return this;
  }

  default FlowOpNode<T> distribute(String name, int count) {
    if(this instanceof FlowDistribNode) {
      return this;
    }
    FlowDistribNode<T> out = new FlowDistribNode<>(name, count);
    FlowNode.link(this, out);
    return out;
  }

  class FlowDistribWriterJob<T> extends FlowJob {
    public FlowDistribWriterJob(int count) {

    }

    @Override
    public FlowSink asSink() {
      return null;
    }

    @Override
    public void execute() throws Exception {

    }
  }

  class FlowDistribNode<T> extends FlowJobNode<FlowDistribWriterJob<T>> implements FlowOpNode<T> {

    public FlowDistribNode(String name, int count) {
      super(name, new FlowDistribWriterJob<T>(count));
    }
  }
}
