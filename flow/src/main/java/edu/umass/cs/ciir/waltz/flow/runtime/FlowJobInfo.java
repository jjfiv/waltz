package edu.umass.cs.ciir.waltz.flow.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jfoley
 */
public class FlowJobInfo {
  public String id;
  public List<String> inputIds;
  public List<String> outputIds;
  public Class<? extends FlowJob> jobClass;
  public byte[] jobState;

  public FlowJobInfo() {
    this.id = null;
    this.inputIds = new ArrayList<>();
    this.outputIds = new ArrayList<>();
    this.jobClass = null;
    this.jobState = null;
  }

  public FlowJob create() {
    try {
      Constructor<? extends FlowJob> cons = jobClass.getDeclaredConstructor();
      cons.setAccessible(true);
      FlowJob job = cons.newInstance();
      if(jobState != null) {
        job.initState(jobState);
      }
      return job;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new FlowStartTaskException(id, e);
    } catch (NoSuchMethodException e) {
      System.err.println(jobClass);
      System.err.println(Arrays.toString(jobClass.getDeclaredConstructors()));
      throw new FlowStartTaskException("Couldn't find a zero-argument constructor in class: "+jobClass+" in id="+id, e);
    } catch (InvocationTargetException e) {
      throw new FlowStartTaskException(id, e);
    }
  }

  public String getIdentifier() {
    return id;
  }
}
