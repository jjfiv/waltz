package edu.umass.cs.ciir.waltz.coders.sorter;

import ciir.jfoley.chai.fn.SinkFn;

import java.io.Closeable;

/**
 * @author jfoley
 */
public interface ClosingSinkFn<T> extends SinkFn<T>, Closeable {
}
