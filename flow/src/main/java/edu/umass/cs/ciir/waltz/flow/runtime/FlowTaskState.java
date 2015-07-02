package edu.umass.cs.ciir.waltz.flow.runtime;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author jfoley
 */
public interface FlowTaskState {
  @Nonnull
  byte[] encode() throws IOException;

  void decode(@Nonnull byte[] state) throws IOException;
}
