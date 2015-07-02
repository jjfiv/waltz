package edu.umass.cs.ciir.waltz.flow;

import ciir.jfoley.chai.collections.list.IntList;
import edu.umass.cs.ciir.waltz.flow.lang.FlowJobNode;
import edu.umass.cs.ciir.waltz.flow.lang.FlowNode;
import edu.umass.cs.ciir.waltz.flow.lang.FlowOpNode;
import edu.umass.cs.ciir.waltz.flow.runtime.*;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author jfoley
 */
public class Flow {
  public HashMap<String, FlowNode> nodes;
  private Flow() {
    nodes = new HashMap<>();
  }

  public void register(FlowNode node) {
    nodes.put(node.getIdentifier(), node);
  }

  public List<FlowNode> sources() {
    List<FlowNode> sources = new ArrayList<>();
    for (FlowNode flowNode : nodes.values()) {
      if(flowNode.getInputs().isEmpty()) {
        sources.add(flowNode);
      }
    }
    return sources;
  }
  public List<FlowNode> sinks() {
    List<FlowNode> sinks = new ArrayList<>();
    for (FlowNode flowNode : nodes.values()) {
      if(flowNode.getOutputs().isEmpty()) {
        sinks.add(flowNode);
      }
    }
    return sinks;
  }

  // Singleton:
  private static Flow _flow = new Flow();
  @Nonnull
  public static Flow instance() { return _flow; }

  public static void main(String[] args) {
    FlowTask<Integer,Integer> x2 = new FlowMapTask<Integer, Integer>() {
      @Override
      protected Integer map(Integer x) throws Exception {
        return x * 2;
      }
    };

    List<Integer> output = new IntList();
    FlowOpNode<Integer> program = FlowJobNode.collection("nums", Arrays.asList(1, 2, 3, 4, 5))
        .map("x2", (x) -> x*2)
        .connect("x2again", x2)
        .collect("results", new FlowSink<Integer>() {
          @Override
          protected void onInput(Integer x) throws Exception {
            output.add(x);
          }
        });

    for (FlowNode node : Flow.instance().nodes.values()) {
      String current = node.getIdentifier();
      for (FlowNode in : node.getInputs()) {
        System.out.printf("%s -> %s\n", in.getIdentifier(), current);
      }
      for (FlowNode out : node.getOutputs()) {
        System.out.printf("%s -> %s\n", current, out.getIdentifier());
      }
    }
    System.out.println(Flow.instance().nodes);

    for (FlowNode node : Flow.instance().sources()) {
      System.out.println("SRC: "+node.getIdentifier());
    }
    for (FlowNode node : Flow.instance().sinks()) {
      System.out.println("SINK: "+node.getIdentifier());
    }

    Map<String, FlowJob> jobs = new HashMap<>();
    Map<String, byte[]> states = new HashMap<>();
    for (FlowNode node : Flow.instance().nodes.values()) {
      String id = node.getIdentifier();
      FlowJob job = node.getJob();
      byte[] state = job.saveState();
      if(state != null) {
        states.put(id, state);
      }
      jobs.put(id, job);
    }
    for (FlowNode node : Flow.instance().nodes.values()) {
      String id = node.getIdentifier();
      // "deserialize" job
      FlowJob current = jobs.get(id);

      // restore state to job
      byte[] state = states.get(id);
      if(state != null) {
        current.initState(state);
      }

      // connect outputs:
      List<FlowJob> outputs = new ArrayList<>();
      for (FlowNode out : node.getOutputs()) {
        outputs.add(jobs.get(out.getIdentifier()));
      }

      if(outputs.size() > 1) {
        throw new RuntimeException("Need to implement multi-output.");
      } else if(outputs.size() == 1) {
        current.connectOutput(outputs.get(0).asSink());
      }
    }

    for (FlowNode node : Flow.instance().sources()) {
      String id = node.getIdentifier();
      FlowJob job = jobs.get(id);
      try {
        System.out.println("RUN: "+id);
        job.execute();
      } catch (Exception e) {
        throw new FlowRuntimeError("Execution: ",e);
      }
    }

    System.out.println(output);

  }



}
