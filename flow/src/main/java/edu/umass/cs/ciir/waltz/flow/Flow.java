package edu.umass.cs.ciir.waltz.flow;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.flow.lang.FlowJobNode;
import edu.umass.cs.ciir.waltz.flow.lang.FlowOpNode;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowRuntimeError;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowSink;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowTask;

import java.util.*;

/**
 * @author jfoley
 */
public class Flow {

  public static abstract class MapTask<Input,Output> extends FlowTask<Input,Output> {

    @Override
    protected void run(Input input, FlowSink<Output> output) {
      try {
        output.process(map(input));
      } catch (Exception e) {
        throw new FlowRuntimeError(input, e);
      }
    }

    protected abstract Output map(Input input) throws Exception;
  }


  public static void main(String[] args) {
    FlowTask<Integer,Integer> x2 = new MapTask<Integer, Integer>() {
      @Override
      protected Integer map(Integer x) throws Exception {
        return x * 2;
      }
    };

    List<Integer> output = new IntList();
    FlowOpNode<Integer> program = FlowJobNode.collection("nums", Arrays.asList(1, 2, 3, 4, 5))
        .connect("x2", x2)
        .collect("results", new FlowSink<Integer>() {
          @Override
          protected void onInput(Integer x) throws Exception {
            output.add(x);
          }
        });

  }


}
