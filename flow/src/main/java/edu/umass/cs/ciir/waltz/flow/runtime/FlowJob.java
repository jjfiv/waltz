package edu.umass.cs.ciir.waltz.flow.runtime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * @author jfoley
 */
public abstract class FlowJob implements AutoCloseable {
  protected FlowSink flowJobOutput = null;
  protected FlowTaskState initialState = null;

  /**
   * Implement this method if your job needs to save/restore from state.
   *
   * @return an object containing the state your job needs to run remotely or on a different JVM.
   */
  @Nullable
  public FlowTaskState getState() {
    return initialState;
  }

  public void initState(@Nonnull byte[] data) {
    initialState = getState();
    assert (initialState != null);
    try {
      initialState.decode(data);
    } catch (IOException e) {
      // TODO hexdump();
      throw new FlowRuntimeError("Error while decoding state in " + this.getClass().getName(), e);
    }
  }

  public byte[] saveState() {
    FlowTaskState state = getState();
    if(state == null) return null;
    try {
      return state.encode();
    } catch (IOException e) {
      throw new FlowRuntimeError("Could not save state in " + this.getClass().getName(), e);
    }
  }

  /**
   * Setup an output pipe attached to this job.
   */
  public void connectOutput(FlowSink sink) {
    this.flowJobOutput = sink;
  }

  /**
   * @return this job as a sink object that accepts input and pipes output correctly to its flowJobOutput, if applicable.
   */
  public abstract FlowSink asSink();

  /**
   * Run this job.
   */
  public abstract void execute() throws Exception;

  @Override
  public void close() throws Exception {
    Exception closingError = null;
    try {
      onClose();
    } catch (Exception e) {
      closingError = e;
    }
    flowJobOutput.close();
    if (closingError != null) {
      throw new FlowRuntimeError("Error while closing: ", closingError);
    }
  }

  /**
   * Override this to do some kind of cleanup.
   *
   * @throws Exception
   */
  protected void onClose() throws Exception {

  }

  public void setState(FlowTaskState state) {
    this.initialState = state;
  }
}
