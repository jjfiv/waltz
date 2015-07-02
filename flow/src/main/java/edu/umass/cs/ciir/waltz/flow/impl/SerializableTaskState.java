package edu.umass.cs.ciir.waltz.flow.impl;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowTaskState;

import javax.annotation.Nonnull;
import java.io.*;

/**
 * @author jfoley
 */
public class SerializableTaskState<T extends Serializable> implements FlowTaskState {
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
