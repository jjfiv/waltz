package edu.umass.cs.ciir.waltz.coders.reduce;

import ciir.jfoley.chai.fn.SinkFn;

import java.io.Closeable;

/**
 * @author jfoley
 */
public interface ClosingSinkFn<T> extends SinkFn<T>, Closeable {
}
