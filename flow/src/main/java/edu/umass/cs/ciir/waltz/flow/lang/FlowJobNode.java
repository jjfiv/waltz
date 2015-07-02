package edu.umass.cs.ciir.waltz.flow.lang;

import edu.umass.cs.ciir.waltz.flow.Flow;
import edu.umass.cs.ciir.waltz.flow.impl.FlowCollectionSource;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowJob;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author jfoley
 */
public abstract class FlowJobNode<Job extends FlowJob> implements FlowNode {
  private String identifier;
  private List<FlowNode> inputs;
  private List<FlowNode> outputs;

  /**
   * The abstract handle to the job itself.
   */
  public Job job;

  protected FlowJobNode(@Nonnull String identifier, Job job) {
    this.identifier = identifier;
    this.inputs = new ArrayList<>();
    this.outputs = new ArrayList<>();
    this.job = job;
    Flow.instance().register(this);
  }

  @Override
  public Job getJob() {
    return job;
  }

  @Nonnull
  public static <T extends Serializable> FlowSourceNode<T> collection(String name, Collection<? extends T> items) {
    return new FlowSourceNode<>(name, new FlowCollectionSource<>(items));
  }

  @Override
  public int hashCode() {
    return this.identifier.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof FlowTaskNode)) return false;
    FlowJobNode<?> rhs = (FlowJobNode<?>) other;

    assert (this.getClass().equals(rhs.getClass()));
    return this.identifier.equals(rhs.identifier) &&
        this.inputs.equals(rhs.inputs) &&
        this.outputs.equals(rhs.outputs);
  }

  @Override
  public void addOutput(FlowNode out) {
    this.outputs.add(out);
  }

  @Override
  public void addInput(FlowNode out) {
    this.inputs.add(out);
  }

  /**
   * Task Identifier:
   */
  @Override
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Input to this node, if any:
   */
  @Override
  public List<FlowNode> getInputs() {
    return inputs;
  }

  /**
   * Outputs from this, if any:
   */
  @Override
  public List<FlowNode> getOutputs() {
    return outputs;
  }
}
