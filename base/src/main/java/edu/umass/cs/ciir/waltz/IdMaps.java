package edu.umass.cs.ciir.waltz;

import edu.umass.cs.ciir.waltz.coders.map.IOMap;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriter;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * @author jfoley.
 */
public class IdMaps {
  public static class Writer<V> implements Flushable, Closeable {
    public final IOMapWriter<Integer, V> forwardWriter;
    public final IOMapWriter<V, Integer> reverseWriter;

    public Writer(IOMapWriter<Integer, V> forwardWriter, IOMapWriter<V, Integer> reverseWriter) throws IOException {
      this.forwardWriter = forwardWriter.getSorting();
      this.reverseWriter = reverseWriter.getSorting();
    }

    public void put(int id, V value) throws IOException {
      forwardWriter.put(id, value);
      reverseWriter.put(value, id);
    }

    @Override
    public void close() throws IOException {
      forwardWriter.close();
      reverseWriter.close();
    }

    @Override
    public void flush() throws IOException {
      forwardWriter.flush();
      reverseWriter.flush();
    }

  }

  public static class Reader<V> implements Closeable {
    public final IOMap<Integer, V> forwardReader;
    public final IOMap<V, Integer> reverseReader;

    public Reader(IOMap<Integer, V> forwardReader, IOMap<V, Integer> reverseReader) {
      this.forwardReader = forwardReader;
      this.reverseReader = reverseReader;
    }

    @Override
    public void close() throws IOException {
      this.forwardReader.close();
      this.reverseReader.close();
    }
  }

}
