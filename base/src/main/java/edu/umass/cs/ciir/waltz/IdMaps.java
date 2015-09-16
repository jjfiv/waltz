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

  public interface IdReader<V> extends Closeable {

    Iterable<Pair<Integer, V>> getForward(List<Integer> bulk) throws IOException;

    V getForward(int id) throws IOException;

    Iterable<Pair<V, Integer>> getReverse(List<V> bulk) throws IOException;

    Integer getReverse(V item) throws IOException;

    Iterable<Pair<Integer, V>> items() throws IOException;

    Iterable<Integer> ids() throws IOException;

    Iterable<V> values() throws IOException;
  }

  public static class Reader<V> implements IdReader<V> {
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

    @Override
    public Iterable<Pair<Integer, V>> getForward(List<Integer> bulk) throws IOException {
      return forwardReader.getInBulk(bulk);
    }
    @Override
    public V getForward(int id) throws IOException {
      return forwardReader.get(id);
    }
    @Override
    public Iterable<Pair<V, Integer>> getReverse(List<V> bulk) throws IOException {
      return reverseReader.getInBulk(bulk);
    }
    @Override
    public Integer getReverse(V item) throws IOException {
      return reverseReader.get(item);
    }
    @Override
    public Iterable<Pair<Integer, V>> items() throws IOException {
      return forwardReader.items();
    }
    @Override
    public Iterable<Integer> ids() throws IOException {
      return forwardReader.keys();
    }
    @Override
    public Iterable<V> values() throws IOException {
      return reverseReader.keys();
    }

    public long size() {
      return forwardReader.keyCount();
    }
  }

  public static class CachedIdReader<V> implements IdReader<V> {
    final IdReader<V> inner;

    public CachedIdReader(IdReader<V> inner) {
      this.inner = inner;
    }

    @Override
    public Iterable<Pair<Integer, V>> getForward(List<Integer> bulk) throws IOException {
      return null;
    }

    @Override
    public V getForward(int id) throws IOException {
      return null;
    }

    @Override
    public Iterable<Pair<V, Integer>> getReverse(List<V> bulk) throws IOException {
      return null;
    }

    @Override
    public Integer getReverse(V item) throws IOException {
      return null;
    }

    @Override
    public Iterable<Pair<Integer, V>> items() throws IOException {
      return null;
    }

    @Override
    public Iterable<Integer> ids() throws IOException {
      return null;
    }

    @Override
    public Iterable<V> values() throws IOException {
      return null;
    }

    @Override
    public void close() throws IOException {

    }
  }

}
