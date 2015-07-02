package edu.umass.cs.ciir.waltz.flow.lang;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowSource;

import javax.annotation.Nonnull;

/**
 * @author jfoley
 */
public class FlowSourceNode<T> extends FlowJobNode<FlowSource<T>> implements FlowOpNode<T> {
  protected FlowSourceNode(@Nonnull String identifier, FlowSource<T> job) {
    super(identifier, job);
  }
}
