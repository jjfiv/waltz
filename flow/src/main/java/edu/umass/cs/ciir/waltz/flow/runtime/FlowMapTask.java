package edu.umass.cs.ciir.waltz.flow.runtime;

/**
 * @author jfoley
 */
public abstract class FlowMapTask<Input, Output> extends FlowTask<Input, Output> {
  @Override
  protected void run(Input input, FlowSink<Output> output) {
    try {
      output.process(map(input));
    } catch (Exception e) {
      throw new FlowRuntimeError(input, e);
    }
  }

  protected abstract Output map(Input input) throws Exception;
}
