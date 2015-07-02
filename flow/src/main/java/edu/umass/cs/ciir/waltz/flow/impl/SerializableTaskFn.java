package edu.umass.cs.ciir.waltz.flow.impl;

import edu.umass.cs.ciir.waltz.flow.lambda.FTaskFn;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowSink;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowTask;

/**
 * @author jfoley
 */
public class SerializableTaskFn<Input, Output> extends FlowTask<Input, Output> implements SerializableStateful<FTaskFn<Input, Output>> {
  private FTaskFn<Input, Output> lambda;

  @Override
  protected void run(Input input, FlowSink<Output> output) throws Exception {
    getState().run(input, output);
  }

  @Override
  public FTaskFn<Input, Output> getState() {
    return lambda;
  }

  @Override
  public void setState(FTaskFn<Input, Output> object) {
    lambda = object;
  }
}
