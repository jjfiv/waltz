package edu.umass.cs.ciir.waltz;

import ciir.jfoley.chai.collections.Pair;
import edu.umass.cs.ciir.waltz.coders.map.IOMap;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriter;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.List;

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
    private final IOMap<Integer, V> forwardReader;
    private final IOMap<V, Integer> reverseReader;

    public Reader(IOMap<Integer, V> forwardReader, IOMap<V, Integer> reverseReader) {
      this.forwardReader = forwardReader;
      this.reverseReader = reverseReader;
    }

    @Override
    public void close() throws IOException {
      this.forwardReader.close();
      this.reverseReader.close();
    }

    public Iterable<Pair<Integer, V>> getForward(List<Integer> bulk) throws IOException {
      return forwardReader.getInBulk(bulk);
    }
    public V getForward(int id) throws IOException {
      return forwardReader.get(id);
    }
    public Iterable<Pair<V, Integer>> getReverse(List<V> bulk) throws IOException {
      return reverseReader.getInBulk(bulk);
    }
    public Integer getReverse(V item) throws IOException {
      return reverseReader.get(item);
    }
    public Iterable<Pair<Integer, V>> items() throws IOException {
      return forwardReader.items();
    }
    public Iterable<Integer> ids() throws IOException {
      return forwardReader.keys();
    }
    public Iterable<V> values() throws IOException {
      return reverseReader.keys();
    }
  }

}
