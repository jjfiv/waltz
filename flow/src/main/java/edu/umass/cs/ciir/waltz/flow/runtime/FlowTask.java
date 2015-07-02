package edu.umass.cs.ciir.waltz.flow.runtime;

/**
 * @author jfoley
 */
public abstract class FlowTask<Input, Output> extends FlowJob {
  /**
   * This method is called and converts any errors to {@link FlowRuntimeError} exceptions
   *
   * @param input  the inputs object given to this task.
   * @param output the output object of this task.
   */
  public final void process(Input input, FlowSink<Output> output) {
    try {
      run(input, output);
    } catch (Exception e) {
      throw new FlowRuntimeError(input, e);
    }
  }

  protected abstract void run(Input input, FlowSink<Output> output) throws Exception;

  @Override
  public void execute() {
    throw new FlowRuntimeError("Can't execute a \"Task\"");
  }
}
