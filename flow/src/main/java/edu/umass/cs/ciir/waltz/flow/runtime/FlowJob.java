package edu.umass.cs.ciir.waltz.flow.runtime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * @author jfoley
 */
public abstract class FlowJob implements AutoCloseable {
  protected FlowSink flowJobOutput = null;

  @Nullable
  public byte[] saveState() {
    if(this instanceof FlowStateful) {
      try {
        return ((FlowStateful) this).encode();
      } catch (IOException e) {
        throw new FlowRuntimeError("Couldn't encode: ", e);
      }
    }
    return null;
  }

  public void initState(@Nonnull byte[] data) {
    if(this instanceof FlowStateful) {
      try {
        ((FlowStateful) this).decode(data);
      } catch (IOException e) {
        throw new FlowRuntimeError("Error while decoding state in " + this.getClass().getName(), e);
      }
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
}
