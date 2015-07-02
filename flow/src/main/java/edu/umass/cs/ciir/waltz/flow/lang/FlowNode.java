package edu.umass.cs.ciir.waltz.flow.lang;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowJob;

import java.util.List;

/**
 * @author jfoley
 */
public interface FlowNode {
  void addOutput(FlowNode out);
  void addInput(FlowNode out);
  String getIdentifier();
  List<FlowNode> getInputs();
  List<FlowNode> getOutputs();

  FlowJob getJob();

  static void link(FlowNode src, FlowNode dest) {
    src.addOutput(dest);
    dest.addInput(src);
  }
}
