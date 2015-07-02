package edu.umass.cs.ciir.waltz.flow.lambda;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowSink;

import java.io.Serializable;

/**
 * @author jfoley
 */
public interface FTaskFn<Input, Output> extends Serializable {
  void run(Input input, FlowSink<Output> output);
}
