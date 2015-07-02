package edu.umass.cs.ciir.waltz.flow.runtime;

import java.util.Collection;

/**
 * @author jfoley
 */
public abstract class FlowSink<Input> extends FlowJob {
  /**
   * This is the method you implement.
   *
   * @param x the item to process with this sink.
   * @throws Exception
   */
  protected abstract void onInput(Input x) throws Exception;

  /**
   * This can be implemented if it's better for efficiency.
   *
   * @param xs the inputs elements to process.
   * @throws Exception so that you don't need to try/catch yourself for fatal things.
   */
  protected void onInputs(Collection<? extends Input> xs) throws Exception {
    for (Input x : xs) {
      process(x);
    }
  }

  /**
   * This is the method you call when outputting data.
   *
   * @param x
   */
  public final void process(Input x) {
    try {
      onInput(x);
    } catch (Exception e) {
      throw new FlowRuntimeError(x, e);
    }
  }

  /**
   * This is the method you call when outputting data.
   *
   * @param x
   */
  public final void process(Collection<? extends Input> x) {
    try {
      onInputs(x);
    } catch (Exception e) {
      throw new FlowRuntimeError(x, e);
    }
  }

  @Override
  public void execute() {
    throw new FlowRuntimeError("Can't execute a \"Sink\"");
  }
}
