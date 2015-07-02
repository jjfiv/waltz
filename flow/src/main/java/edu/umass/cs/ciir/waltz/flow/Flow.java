package edu.umass.cs.ciir.waltz.flow;

import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.lang.LazyPtr;
import edu.umass.cs.ciir.waltz.flow.lambda.FMapFn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

/**
 * @author jfoley
 */
public class Flow {

  public static abstract class FlowJob implements AutoCloseable {
    protected Sink flowJobOutput = null;
    protected TaskState initialState = null;

    /**
     * Implement this method if your job needs to save/restore from state.
     * @return an object containing the state your job needs to run remotely or on a different JVM.
     */
    @Nullable public TaskState getState() { return null; }

    /**
     * Helper function: is this job stateful?
     * @return true if this job is stateful.
     */
    public final boolean hasState() { return getState() != null; }
    public void initState(@Nonnull byte[] data) {
      initialState = getState();
      assert(initialState != null);
      try {
        initialState.decode(data);
      } catch (IOException e) {
        // TODO hexdump();
        throw new FlowRuntimeError("Error while decoding state in "+this.getClass().getName(), e);
      }
    }
    public byte[] saveState() {
      TaskState state = getState();
      assert(state != null);
      try {
        return state.encode();
      } catch (IOException e) {
        throw new FlowRuntimeError("Could not save state in "+this.getClass().getName(), e);
      }
    }

    /** Setup an output pipe attached to this job. */
    public void connectOutput(Sink sink) {
      this.flowJobOutput = sink;
    }

    /** Run this job.*/
    public abstract void execute() throws Exception;

    @Override
    public void close() throws Exception {
      Exception closingError = null;
      try {
        onClose();
      } catch (Exception e) {
        closingError = e;
      }
      flowJobOutput.close();
      if(closingError != null) {
        throw new FlowRuntimeError("Error while closing: ",closingError);
      }
    }

    /**
     * Override this to do some kind of cleanup.
     * @throws Exception
     */
    protected void onClose() throws Exception {

    }
  }

  public static abstract class Sink<Input> extends FlowJob {
    /**
     * This is the method you implement.
     * @param x the item to process with this sink.
     * @throws Exception
     */
    protected abstract void onInput(Input x) throws Exception;

    /**
     * This can be implemented if it's better for efficiency.
     * @param xs the inputs elements to process.
     * @throws Exception so that you don't need to try/catch yourself for fatal things.
     */
    protected void onInputs(Collection<? extends Input> xs) throws Exception {
      for (Input x : xs) {
        process(x);
      }
    }

    /**
     * This is the method you call when outputting data.
     * @param x
     */
    public final void process(Input x) {
      try {
        onInput(x);
      } catch (Exception e) {
        throw new FlowRuntimeError(x, e);
      }
    }

    /**
     * This is the method you call when outputting data.
     * @param x
     */
    public final void process(Collection<? extends Input> x) {
      try {
        onInputs(x);
      } catch (Exception e) {
        throw new FlowRuntimeError(x, e);
      }
    }

    @Override
    public void execute() {
      throw new FlowRuntimeError("Can't execute a \"Sink\"");
    }
  }

  public static abstract class FlowTask<Input,Output> extends FlowJob {
    /**
     * This method is called and converts any errors to {@link FlowRuntimeError} exceptions
     * @param input the inputs object given to this task.
     * @param output the output object of this task.
     */
    public final void process(Input input, Sink<Output> output) {
      try {
        run(input, output);
      } catch (Exception e) {
        throw new FlowRuntimeError(input, e);
      }
    }

    protected abstract void run(Input input, Sink<Output> output) throws Exception;

    @Override
    public void execute() {
      throw new FlowRuntimeError("Can't execute a \"Task\"");
    }
  }

  public static abstract class Source<Output> extends FlowJob {
    public abstract void run(Sink<Output> output) throws Exception;
    @Override
    public void execute() throws Exception {
      assert(this.flowJobOutput != null);
    }
  }

  public static abstract class MapTask<Input,Output> extends FlowTask<Input,Output> {

    @Override
    protected void run(Input input, Sink<Output> output) {
      try {
        output.process(map(input));
      } catch (Exception e) {
        throw new FlowRuntimeError(input, e);
      }
    }

    protected abstract Output map(Input input) throws Exception;
  }

  public interface TaskState {
    @Nonnull byte[] encode() throws IOException;
    void decode(@Nonnull byte[] state) throws IOException;
  }

  public static class SerializableTaskState<T extends Serializable> implements TaskState {
    private T object;
    public SerializableTaskState(@Nonnull T object) {
      this.object = object;
    }
    public T get() {
      return object;
    }

    @Nonnull
    @Override
    public byte[] encode() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
        oos.writeObject(object);
      }
      return baos.toByteArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void decode(@Nonnull byte[] state) throws IOException {
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(state));
      try {
        this.object = (T) ois.readObject();
      } catch (ClassNotFoundException e) {
        throw new IOException(e);
      }

    }
  }

  public static class NodeSavedState {
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

  public static abstract class SerializableTask<T extends Serializable, Input,Output> extends FlowTask<Input,Output> {
    private SerializableTaskState<T> state;

    public SerializableTask(T item) {
      this.state = new SerializableTaskState<>(item);
    }

    @Override
    public TaskState getState() {
      return state;
    }

    @SuppressWarnings("unchecked")
    LazyPtr<T> item = new LazyPtr<>(this.state::get);
  }

  public interface INode {
    void addOutput(INode out);

    void addInput(INode out);

    String getIdentifier();

    List<INode> getInputs();

    List<INode> getOutputs();

    static void link(INode src, INode dest) {
      src.addOutput(dest);
      dest.addInput(src);
    }
  }

  public static abstract class AbstractNode<Job extends FlowJob> implements INode {
    private String identifier;
    private List<INode> inputs;
    private List<INode> outputs;

    /** The abstract handle to the job itself. */
    public Job job;

    protected AbstractNode(@Nonnull String identifier, Job job) {
      this.identifier = identifier;
      this.inputs = null;
      this.outputs = new ArrayList<>();
      this.job = job;
    }

    @Nonnull
    public static <T> SourceNode<T> collection(String name, Collection<? extends T> items) {
      return new SourceNode<T>(name, new Source<T>() {
        @Override
        public void run(Sink<T> output) throws Exception {

        }
      });
    }

    public NodeSavedState save() {
      NodeSavedState nss = new NodeSavedState();
      nss.id = Objects.requireNonNull(this.identifier);
      for (INode input : inputs) {
        nss.inputIds.add(input.getIdentifier());
      }
      for (INode output : outputs) {
        nss.outputIds.add(output.getIdentifier());
      }

      nss.jobClass = job.getClass();
      nss.jobState = job.saveState();
      return nss;
    }

    @Override
    public int hashCode() {
      return this.identifier.hashCode();
    }

    @Override
    public boolean equals(Object other) {
      if(!(other instanceof Node)) return false;
      AbstractNode<?> rhs = (AbstractNode<?>) other;

      assert(this.getClass().equals(rhs.getClass()));
      return this.identifier.equals(rhs.identifier) &&
          this.inputs.equals(rhs.inputs) &&
          this.outputs.equals(rhs.outputs);
    }

    @Override
    public void addOutput(INode out) {
      this.outputs.add(out);
    }

    @Override
    public void addInput(INode out) {
      this.inputs.add(out);
    }

    /** Task Identifier: */
    @Override
    public String getIdentifier() {
      return identifier;
    }

    /** Input to this node, if any: */
    @Override
    public List<INode> getInputs() {
      return inputs;
    }

    /** Outputs from this, if any: */
    @Override
    public List<INode> getOutputs() {
      return outputs;
    }
  }

  /** Chainable, stream-like operations for SourceNode and TaskNode */
  public interface OpNode<T> extends INode {
    default <X> OpNode<X> connect(@Nonnull String name, @Nonnull FlowTask<T, X> task) {
      Node<X> next = new Node<>(name, task);
      this.addOutput(next);
      return next;
    }
    default <X> OpNode<X> map(@Nonnull String name, FMapFn<T, X> mapper) {
      FlowTask<T,X> mapperTask = new SerializableTask<FMapFn<T,X>, T, X>(mapper) {
        @Override
        protected void run(T o, Sink<X> output) throws Exception {
          output.process(item.get().map(o));
        }
      };
      Node<X> next = new Node<>(name, mapperTask);
      this.addOutput(next);
      return next;
    }
    default OpNode<T> collect(String name, Sink<T> sink) {
      addOutput(new SinkNode<>(name, sink));
      return this;
    }
  }

  /**
   * All points in the graph inherit from this, where T is the output class, or the type of the items if the node's output were viewed as a collection.
   * @param <T> the type of objects output from this node.
   */
  public static class Node<T> extends AbstractNode<FlowTask<?, T>> implements OpNode<T> {
    /** Task description for this node. */
    public Node(@Nonnull String identifier, @Nonnull FlowTask<?, T> task) {
      super(identifier, task);
    }

  }

  public static class SourceNode<T> extends AbstractNode<Source<T>> implements OpNode<T> {
    protected SourceNode(@Nonnull String identifier, Source<T> job) {
      super(identifier, job);
    }
  }

  /**
   * A dead-end node?
   * @param <T>
   */
  public static class SinkNode<T> extends AbstractNode<Sink<T>> {
    protected SinkNode(@Nonnull String identifier, Sink<T> sink) {
      super(identifier, sink);
    }
  }


  public static void main(String[] args) {
    FlowTask<Integer,Integer> x2 = new MapTask<Integer, Integer>() {
      @Override
      protected Integer map(Integer x) throws Exception {
        return x * 2;
      }
    };

    List<Integer> output = new IntList();
    OpNode<Integer> program = AbstractNode.collection("nums", Arrays.asList(1, 2, 3, 4, 5))
        .connect("x2", x2)
        .collect("results", new Sink<Integer>() {
          @Override
          protected void onInput(Integer x) throws Exception {
            output.add(x);
          }
        });

  }


}
