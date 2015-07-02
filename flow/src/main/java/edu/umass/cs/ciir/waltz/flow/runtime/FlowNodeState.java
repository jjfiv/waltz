package edu.umass.cs.ciir.waltz.flow.runtime;

import java.util.List;

/**
 * @author jfoley
 */
public class FlowNodeState {
  public String id;
  public List<String> inputIds;
  public List<String> outputIds;
  public Class<? extends FlowJob> jobClass;
  public byte[] jobState;

  public FlowJob createJob() {
    try {
      FlowJob job = jobClass.newInstance();
      job.initState(jobState);
      return job;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new FlowStartTaskException(e);
    }
  }
}
