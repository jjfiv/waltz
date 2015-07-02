package edu.umass.cs.ciir.waltz.flow.runtime;

import edu.umass.cs.ciir.waltz.flow.lang.FlowASTGraph;

import java.util.*;

/**
 * @author jfoley
 */
public class FlowRunner implements Runnable {
  private final Map<String, FlowJobInfo> graph;
  public Map<String, FlowJob> jobs;
  public Map<String, byte[]> states;
  public Map<String, FlowJob> sources;

  public FlowRunner(FlowASTGraph graph) {
    this(graph.asJobInfoGraph());
  }
  public FlowRunner(Collection<? extends FlowJobInfo> descriptions) {
    this.graph = new HashMap<>();
    this.jobs = new HashMap<>();
    this.states = new HashMap<>();

    for (FlowJobInfo desc : descriptions) {
      FlowJob job = desc.create();

      this.graph.put(desc.id, desc);
      this.jobs.put(desc.id, job);
      int numInputs = desc.inputIds.size();
      int numOutputs = desc.outputIds.size();

      if(numInputs == 0 && numOutputs > 0) {
        sources.put(desc.id, job);
      }
    }
  }

  public void linkOutputJobs() {
    for (FlowJobInfo node : this.graph.values()) {
      String id = node.getIdentifier();
      // "deserialize" job
      FlowJob current = jobs.get(id);

      // find outputs:
      List<FlowJob> outputs = new ArrayList<>();
      for (String outputId : node.outputIds) {
        FlowJob output = jobs.get(outputId);
        if(output == null) {
          throw new FlowRuntimeError("Missing output job id="+outputId);
        }
        outputs.add(output);
      }

      if(outputs.size() > 1) {
        throw new RuntimeException("Need to implement multi-output.");
      } else if(outputs.size() == 1) {
        current.connectOutput(outputs.get(0).asSink());
      }
    }
  }

  public Collection<? extends FlowJobInfo> nodes() {
    return graph.values();
  }

  public void run() {
    for (Map.Entry<String,FlowJob> job : sources.entrySet()) {
      try {
        System.out.println("RUN: "+job.getKey());
        job.getValue().execute();
      } catch (Exception e) {
        throw new FlowRuntimeError("Execution: ",e);
      }
    }
  }
}
