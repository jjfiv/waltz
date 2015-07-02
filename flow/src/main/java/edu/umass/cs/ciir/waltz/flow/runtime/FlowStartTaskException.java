package edu.umass.cs.ciir.waltz.flow.runtime;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowRuntimeError;

/**
 * @author jfoley
 */
public class FlowStartTaskException extends FlowRuntimeError {
  public FlowStartTaskException(Exception e) {
    super("Could not start task: ", e);
  }
}
