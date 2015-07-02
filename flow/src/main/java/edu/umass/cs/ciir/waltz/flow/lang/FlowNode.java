package edu.umass.cs.ciir.waltz.flow.lang;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowJob;
import edu.umass.cs.ciir.waltz.flow.runtime.FlowJobInfo;

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

  default FlowJobInfo getInfo() {
    FlowJobInfo info = new FlowJobInfo();
    info.id = getIdentifier();
    for (FlowNode input : getInputs()) {
      info.inputIds.add(input.getIdentifier());
    }
    for (FlowNode output : getOutputs()) {
      info.outputIds.add(output.getIdentifier());
    }

    FlowJob job = getJob();
    info.jobClass = job.getClass();
    info.jobState = job.saveState();

    return info;
  }


  static void link(FlowNode src, FlowNode dest) {
    src.addOutput(dest);
    dest.addInput(src);
  }
}
