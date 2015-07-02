package edu.umass.cs.ciir.waltz.flow.impl;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowStateful;
import org.lemurproject.galago.utility.Parameters;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * A "mixin" that encodes state as JSON and Flow can move your job around as necessary.
 * @author jfoley
 */
public interface ParametersStateful extends FlowStateful {
  Parameters getCfg();
  void setCfg(Parameters cfg);

  @Nonnull
  @Override
  default byte[] encode() throws IOException {
    return getCfg().toString().getBytes("UTF-8");
  }

  @Override
  @SuppressWarnings("unchecked")
  default void decode(@Nonnull byte[] state) throws IOException {
    setCfg(Parameters.parseString(new String(state, "UTF-8")));
  }
}
