package edu.umass.cs.ciir.waltz.flow.runtime;

/**
 * @author jfoley
 */
public abstract class FlowSource<Output> extends FlowJob {
  public abstract void run(FlowSink<Output> output) throws Exception;

  @Override
  public void execute() throws Exception {
    assert (this.flowJobOutput != null);
  }
}
