package edu.umass.cs.ciir.waltz.flow.lambda;

import java.io.Serializable;

/**
 * Serializable Mapping function:
 * @author jfoley
 */
public interface FMapFn<A,B> extends Serializable {
  B map(A input) throws Exception;
}
