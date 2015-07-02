package edu.umass.cs.ciir.waltz.flow.lang;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowTask;

import javax.annotation.Nonnull;

/**
 * All points in the graph inherit from this, where T is the output class, or the type of the items if the node's output were viewed as a collection.
 * @param <T> the type of objects output from this node.
 */
public class FlowTaskNode<T> extends FlowJobNode<FlowTask<?, T>> implements FlowOpNode<T> {
  /** Task description for this node. */
  public FlowTaskNode(@Nonnull String identifier, @Nonnull FlowTask<?, T> task) {
    super(identifier, task);
  }

}
