package edu.umass.cs.ciir.waltz.flow;

import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.io.Directory;
import edu.umass.cs.ciir.waltz.flow.lang.FlowASTGraph;
import edu.umass.cs.ciir.waltz.flow.lang.FlowJobNode;
import edu.umass.cs.ciir.waltz.flow.lang.FlowNode;
import edu.umass.cs.ciir.waltz.flow.lang.FlowOpNode;
import edu.umass.cs.ciir.waltz.flow.runtime.*;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * @author jfoley
 */
public class Flow {
  FlowASTGraph graph;
  Directory outputDir;
  private Flow() {
    graph = new FlowASTGraph();
  }

  public void register(FlowNode node) {
    graph.add(node);
  }

  public static List<FlowJobInfo> compile() {
    return instance().graph.asJobInfoGraph();
  }

  // Singleton:
  private static Flow _flow = new Flow();
  @Nonnull
  public static Flow instance() { return _flow; }

  public static List<Integer> output = new IntList();

  public static void main(String[] args) {
    Flow.instance().outputDir = new Directory("flow.out");

    FlowTask<Integer,Integer> x2 = new FlowMapTask<Integer, Integer>() {
      @Override
      protected Integer map(Integer x) throws Exception {
        return x * 2;
      }
    };

    FlowOpNode<Integer> program = FlowJobNode.collection("nums", Arrays.asList(1, 2, 3, 4, 5))
        .map("x2", (x) -> x * 2)
        .connect("x2again", x2)
        .collect("results", new FlowSink<Integer>() {
          @Override
          protected void onInput(Integer x) throws Exception {
            output.add(x);
          }
        });

    List<FlowJobInfo> steps = Flow.compile();

    FlowRunner runner = new FlowRunner(steps);
    runner.run();

    System.out.println(output);

  }



}
