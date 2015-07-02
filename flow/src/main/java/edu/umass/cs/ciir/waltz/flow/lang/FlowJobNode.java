package edu.umass.cs.ciir.waltz.flow.lang;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowJob;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowNodeState;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowSink;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowSource;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
    this.inputs = null;
    this.outputs = new ArrayList<>();
    this.job = job;
  }

  @Nonnull
  public static <T> FlowSourceNode<T> collection(String name, Collection<? extends T> items) {
    return new FlowSourceNode<T>(name, new FlowSource<T>() {
      @Override
      public void run(FlowSink<T> output) throws Exception {

      }
    });
  }

  public FlowNodeState save() {
    FlowNodeState nss = new FlowNodeState();
    nss.id = Objects.requireNonNull(this.identifier);
    for (FlowNode input : inputs) {
      nss.inputIds.add(input.getIdentifier());
    }
    for (FlowNode output : outputs) {
      nss.outputIds.add(output.getIdentifier());
    }

    nss.jobClass = job.getClass();
    nss.jobState = job.saveState();
    return nss;
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
