package edu.umass.cs.ciir.waltz.flow.runtime;

import javax.annotation.Nonnull;

/**
 * @author jfoley
 */
public abstract class FlowSource<Output> extends FlowJob {
  public abstract void run(FlowSink<Output> output) throws Exception;

  @Override
  public void execute() throws Exception {
    assert (this.flowJobOutput != null);
    this.run(getNextStep());
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  private FlowSink<Output> getNextStep() {
    assert(flowJobOutput != null);
    return (FlowSink<Output>) flowJobOutput;
  }

  @Override
  public FlowSink asSink() {
    throw new FlowRuntimeError("Can't interpret a FlowSource as a FlowSink!");
  }
}
