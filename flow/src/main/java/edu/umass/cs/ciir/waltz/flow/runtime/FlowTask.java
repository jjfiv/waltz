package edu.umass.cs.ciir.waltz.flow.runtime;

import javax.annotation.Nonnull;
import java.util.Collection;

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
  public void process(Input input, FlowSink<Output> output) {
    try {
      run(input, output);
    } catch (Exception e) {
      throw new FlowRuntimeError(input, e);
    }
  }

  protected abstract void run(Input input, FlowSink<Output> output) throws Exception;

  @Override
  public FlowSink<Input> asSink() {
    assert(flowJobOutput != null);

    final FlowSink<Output> next = getNextStep();
    final FlowTask<Input,Output> that = this;
    return new FlowSink<Input>() {
      @Override
      protected void onInput(Input x) throws Exception {
        that.process(x, next);
      }
      @Override
      protected void onInputs(@Nonnull Collection<? extends Input> xs) throws Exception {
        for (Input x : xs) {
          that.process(x, next);
        }
      }
    };
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  private FlowSink<Output> getNextStep() {
    assert(flowJobOutput != null);
    return (FlowSink<Output>) flowJobOutput;
  }

  @Override
  public void execute() {
    throw new FlowRuntimeError("Can't execute a \"Task\"");
  }
}
