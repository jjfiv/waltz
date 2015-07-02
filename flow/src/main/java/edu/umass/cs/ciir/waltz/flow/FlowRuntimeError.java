package edu.umass.cs.ciir.waltz.flow;

/**
 * @author jfoley
 */
public class FlowRuntimeError extends RuntimeException {

  public FlowRuntimeError(Object x, Exception e) { super("Error caused by object={" + x + "}", e); }
  public FlowRuntimeError(String msg, Exception e) { super(msg, e); }
}
