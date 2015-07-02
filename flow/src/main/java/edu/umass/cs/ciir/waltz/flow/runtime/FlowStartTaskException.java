package edu.umass.cs.ciir.waltz.flow.runtime;

/**
 * @author jfoley
 */
public class FlowStartTaskException extends FlowRuntimeError {
  public FlowStartTaskException(String id, Exception e) {
    super("Could not start task id="+id, e);
  }
}
