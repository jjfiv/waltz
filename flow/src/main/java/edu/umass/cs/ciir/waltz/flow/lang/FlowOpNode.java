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
    this.addOutput(next);
    return next;
  }
  default <X> FlowOpNode<X> map(@Nonnull String name, FMapFn<T, X> mapper) {
    FlowTask<T,X> mapperTask = new SerializableTask<FMapFn<T,X>, T, X>(mapper) {
      @Override
      protected void run(T o, FlowSink<X> output) throws Exception {
        output.process(item.get().map(o));
      }
    };
    FlowTaskNode<X> next = new FlowTaskNode<>(name, mapperTask);
    this.addOutput(next);
    return next;
  }
  default FlowOpNode<T> collect(String name, FlowSink<T> sink) {
    addOutput(new FlowSinkNode<>(name, sink));
    return this;
  }
}
