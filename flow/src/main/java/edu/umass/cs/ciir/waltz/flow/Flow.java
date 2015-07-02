package edu.umass.cs.ciir.waltz.flow;

import ciir.jfoley.chai.lang.LazyPtr;
import edu.umass.cs.ciir.waltz.flow.lambda.FMapFn;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;

/**
 * @author jfoley
 */
public class Flow {

  public static abstract class Sink<Input> {
    /**
     * This is the method you implement.
     * @param x the item to process with this sink.
     * @throws Exception
     */
    protected abstract void onInput(Input x) throws Exception;

    /**
     * This can be implemented if it's better for efficiency.
     * @param xs the input elements to process.
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
  }

  public static abstract class Source<Output> {

  }

  public static abstract class Task<Input,Output> {
    TaskState state;
    public Task() {
      this.state = null;
    }
    public Task(TaskState state) {
      this.state = state;
    }

    public boolean hasState() {
      return state != null;
    }
    public void initState(@Nonnull byte[] data) {
      assert(hasState());
      try {
        state.decode(data);
      } catch (IOException e) {
        // TODO hexdump();
        throw new FlowRuntimeError("Error while decoding state in "+this.getClass().getName(), e);
      }
    }
    public byte[] saveState() {
      try {
        return getState().encode();
      } catch (IOException e) {
        throw new FlowRuntimeError("Could not save state in "+this.getClass().getName(), e);
      }
    }
    public TaskState getState() {
      return state;
    }

    /**
     * This method is called and converts any errors to {@link FlowRuntimeError} exceptions
     * @param input the input object given to this task.
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
  }

  public static abstract class MapTask<Input,Output> extends Task<Input,Output> {

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
    String id;
    String inputId;
    List<String> outputIds;
    Class<? extends Task> taskClass;
    byte[] taskState;

    public Task createTask() {
      try {
        Task task = taskClass.newInstance();
        task.initState(taskState);
        return task;
      } catch (InstantiationException | IllegalAccessException e) {
        throw new FlowStartTaskException(e);
      }
    }
  }

  public static abstract class SerializableTask<T extends Serializable, Input,Output> extends Task<Input,Output> {
    public SerializableTask(T item) {
      super(new SerializableTaskState<>(item));
    }

    @SuppressWarnings("unchecked")
    LazyPtr<T> item = new LazyPtr<>(((SerializableTaskState<T>) this.state)::get);
  }

  /**
   * All points in the graph inherit from this, where T is the output class, or the type of the items if the node's output were viewed as a collection.
   * @param <Output> the type of objects output from this node.
   */
  public static class Node<Output> {
    /** Task Identifier: */
    public String identifier;
    /** Input to this node, if any: */
    public Node<?> input;
    /** Outputs from this, if any: */
    public List<Node<?>> outputs;
    /** Task description for this node. */
    public Task<?,Output> task;

    protected Node() {
      input = null;
      outputs = new ArrayList<>();
    }
    public Node(@Nonnull String identifier, @Nonnull Task<?,Output> task) {
      this();
      this.identifier = Objects.requireNonNull(identifier);
      this.task = task;
    }

    public NodeSavedState save() {
      NodeSavedState nss = new NodeSavedState();
      nss.id = Objects.requireNonNull(this.identifier);
      if(input != null) {
        nss.inputId = input.identifier;
      }
      for (Node<?> output : outputs) {
        nss.outputIds.add(output.identifier);
      }

      nss.taskClass = task.getClass();
      nss.taskState = task.saveState();
      return nss;
    }

    public <T> Node<T> connect(@Nonnull String name, @Nonnull Task<Output, T> task) {
      Node<T> next = new Node<>(name, task);
      this.outputs.add(next);
      return next;
    }

    public <T> Node<T> map(@Nonnull String name, FMapFn<Output, T> mapper) {
      return connect(name, new SerializableTask<FMapFn<Output,T>, Output, T>(mapper) {
        @Override
        protected void run(Output o, Sink<T> output) throws Exception {
          output.process(item.get().map(o));
        }
      });
    }

    @Override
    public int hashCode() {
      return this.identifier.hashCode();
    }

    @Override
    public boolean equals(Object other) {
      if(!(other instanceof Node)) return false;
      Node rhs = (Node) other;

      return this.identifier.equals(rhs.identifier) &&
          this.input.equals(rhs.input) &&
          this.outputs.equals(rhs.outputs) &&
          this.task.equals(rhs.task);
    }

    public static <T> Node<T> collection(String name, Collection<? extends T> items) {
      return new Node<>(name, new YieldCollectionTask<>(items));
    }
  }

  public static class YieldCollectionTask<T> extends Task<Void, T> {
    Collection<? extends T> items;
    public YieldCollectionTask(Collection<? extends T> items) {
      this.items = items;
    }

    @Override
    protected void run(Void ignored, Sink<T> output) throws Exception {
      output.process(items);
    }
  }

  public static void main(String[] args) {
    Node<Integer> program = Node.collection("nums", Arrays.asList(1,2,3,4,5))
        .connect("x2", new MapTask<Integer, Integer>() {
          @Override
          protected Integer map(Integer x) throws Exception {
            return x*2;
          }
        });
  }


}
