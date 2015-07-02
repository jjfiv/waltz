package edu.umass.cs.ciir.waltz.flow.lang;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowJobInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @author jfoley
 */
public class FlowASTGraph {
  private HashMap<String, FlowNode> nodes;
  public FlowASTGraph() {
    this.nodes = new HashMap<>();
  }
  public FlowASTGraph(Collection<? extends FlowNode> nodes) {
    this();
    for (FlowNode node : nodes) {
      this.nodes.put(node.getIdentifier(), node);
    }
  }

  public void add(FlowNode node) {
    this.nodes.put(node.getIdentifier(), node);
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

  public List<FlowJobInfo> asJobInfoGraph() {
    ArrayList<FlowJobInfo> jobs = new ArrayList<>(nodes.size());
    for (FlowNode node : nodes()) {
      jobs.add(node.getInfo());
    }
    return jobs;
  }


  public Collection<? extends FlowNode> nodes() {
    return nodes.values();
  }

}
