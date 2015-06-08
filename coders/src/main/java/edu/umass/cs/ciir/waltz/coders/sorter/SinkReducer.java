package edu.umass.cs.ciir.waltz.coders.sorter;

import java.io.IOException;

/**
 * @author jfoley
 */
public class SinkReducer<T> implements ClosingSinkFn<T> {
  private final Reducer<T> reducer;
  private final ClosingSinkFn<T> output;
  private T previous;
  public SinkReducer(Reducer<T> reducer, ClosingSinkFn<T> output) {
    this.reducer = reducer;
    this.output = output;
    this.previous = null;
  }
  @Override
  public void process(T input) {
    if(previous == null) {
      previous = input;
    } else {
      if(reducer.shouldMerge(previous, input)) {
        previous = reducer.merge(previous, input);
      } else {
        output.process(previous);
        previous = input;
      }
    }
  }

  @Override
  public void close() throws IOException {
    if(previous != null) {
      output.process(previous);
    }
    output.close();
  }
}
