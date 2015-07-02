package edu.umass.cs.ciir.waltz.flow.lang;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowSink;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowTask;
import edu.umass.cs.ciir.waltz.flow.impl.SerializableTask;
import edu.umass.cs.ciir.waltz.flow.lambda.FMapFn;

import javax.annotation.Nonnull;

/** Chainable, stream-like operations for SourceNode and TaskNode */
public interface FlowOpNode<T> extends FlowNode {
  default <X> FlowOpNode<X> connect(@Nonnull String name, @Nonnull FlowTask<T, X> task) {
    FlowTaskNode<X> next = new FlowTaskNode<>(name, task);
    FlowNode.link(this, next);
    return next;
  }
  default <X> FlowOpNode<X> map(@Nonnull String name, @Nonnull FMapFn<T, X> mapper) {
    FlowTask<T,X> mapperTask = new SerializableTask<FMapFn<T,X>, T, X>(mapper) {
      @Override
      protected void run(T o, FlowSink<X> output) throws Exception {
        output.process(getItem().map(o));
      }
    };
    FlowTaskNode<X> next = new FlowTaskNode<>(name, mapperTask);
    FlowNode.link(this, next);
    return next;
  }
  default FlowOpNode<T> collect(String name, FlowSink<T> sink) {
    FlowNode.link(this, new FlowSinkNode<>(name, sink));
    return this;
  }
}
