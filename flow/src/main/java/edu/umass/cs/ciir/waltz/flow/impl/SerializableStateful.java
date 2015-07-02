package edu.umass.cs.ciir.waltz.flow.impl;

import edu.umass.cs.ciir.waltz.flow.runtime.FlowStateful;

import javax.annotation.Nonnull;
import java.io.*;

/**
 * @author jfoley
 */
public interface SerializableStateful<T extends Serializable> extends FlowStateful {
  T getState();
  void setState(T object);

  @Nonnull
  @Override
  default byte[] encode() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(getState());
    }
    return baos.toByteArray();
  }

  @Override
  @SuppressWarnings("unchecked")
  default void decode(@Nonnull byte[] state) throws IOException {
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(state));
    try {
      setState((T) ois.readObject());
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    }

  }
}
