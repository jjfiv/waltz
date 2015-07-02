package edu.umass.cs.ciir.waltz.flow;

/**
 * @author jfoley
 */
public class FlowStartTaskException extends FlowRuntimeError {
  public FlowStartTaskException(Exception e) {
    super("Could not start task: ", e);
  }
}
