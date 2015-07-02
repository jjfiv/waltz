package edu.umass.cs.ciir.waltz.flow.lang;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowSink;

import javax.annotation.Nonnull;

/**
 * A dead-end node?
 * @param <T>
 */
public class FlowSinkNode<T> extends FlowJobNode<FlowSink<T>> {
  protected FlowSinkNode(@Nonnull String identifier, FlowSink<T> sink) {
    super(identifier, sink);
  }
}
