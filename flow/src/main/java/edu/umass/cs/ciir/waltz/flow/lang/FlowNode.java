package edu.umass.cs.ciir.waltz.flow.lang;

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

  static void link(FlowNode src, FlowNode dest) {
    src.addOutput(dest);
    dest.addInput(src);
  }
}
