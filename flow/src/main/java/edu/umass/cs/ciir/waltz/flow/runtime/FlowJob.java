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
    return null;
  }

  /**
   * Helper function: is this job stateful?
   *
   * @return true if this job is stateful.
   */
  public final boolean hasState() {
    return getState() != null;
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
    assert (state != null);
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
}
