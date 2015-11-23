package edu.umass.cs.ciir.waltz;

import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.collections.util.IterableFns;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import edu.umass.cs.ciir.waltz.coders.map.IOMap;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Nonnull
    Iterable<Pair<Integer, V>> getForward(List<Integer> bulk) throws IOException;
    @Nonnull
    default Map<Integer,V> getForwardMap(List<Integer> bulk) throws IOException {
      HashMap<Integer,V> mapping = new HashMap<>();
      for (Pair<Integer, V> item : getForward(bulk)) {
        mapping.put(item.getKey(), item.getValue());
      }
      return mapping;
    }

    @Nullable
    V getForward(int id) throws IOException;

    @Nonnull
    Iterable<Pair<V, Integer>> getReverse(List<V> bulk) throws IOException;

    @Nullable
    Integer getReverse(V item) throws IOException;
    @Nonnull default Map<V,Integer> getReverseMap(List<V> bulk) throws IOException {
      HashMap<V,Integer> mapping = new HashMap<>();
      for (Pair<V,Integer> item : getReverse(bulk)) {
        mapping.put(item.getKey(), item.getValue());
      }
      return mapping;
    }

    @Nonnull Iterable<Pair<Integer, V>> items() throws IOException;

    @Nonnull Iterable<Integer> ids() throws IOException;

    @Nonnull Iterable<V> values() throws IOException;

    IdReader<V> getCached(long count);
    default IdReader<V> getCached() {
      return getCached(100_000);
    }

    default IntList translateReverse(List<V> objs, int missing) throws IOException {
      Map<V, Integer> reverseMap = this.getReverseMap(objs);
      IntList output = new IntList(objs.size());
      for (V obj : objs) {
        output.push(reverseMap.getOrDefault(obj, missing));
      }
      return output;
    }
    default List<V> translateForward(IntList ids, V missing) throws IOException {
      Map<Integer, V> fwdMap = this.getForwardMap(ids);
      List<V> output = new ArrayList<>(ids.size());
      for (int id : ids) {
        output.add(fwdMap.getOrDefault(id, missing));
      }
      return output;
    }

    long size();
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

    @Nonnull
    @Override
    public Iterable<Pair<Integer, V>> getForward(List<Integer> bulk) throws IOException {
      return forwardReader.getInBulk(bulk);
    }
    @Override
    public V getForward(int id) throws IOException {
      return forwardReader.get(id);
    }
    @Nonnull
    @Override
    public Iterable<Pair<V, Integer>> getReverse(List<V> bulk) throws IOException {
      return reverseReader.getInBulk(bulk);
    }
    @Override
    public Integer getReverse(V item) throws IOException {
      return reverseReader.get(item);
    }
    @Nonnull
    @Override
    public Iterable<Pair<Integer, V>> items() throws IOException {
      return forwardReader.items();
    }
    @Nonnull
    @Override
    public Iterable<Integer> ids() throws IOException {
      return forwardReader.keys();
    }
    @Nonnull
    @Override
    public Iterable<V> values() throws IOException {
      return reverseReader.keys();
    }

    @Override
    public IdReader<V> getCached(long count) {
      return new CachedIdReader<>(this, count);
    }

    public long size() {
      return forwardReader.keyCount();
    }
  }

  public static class CachedIdReader<V> implements IdReader<V> {
    final IdReader<V> inner;
    final LoadingCache<Integer,V> forwardCache;
    final LoadingCache<V,Integer> reverseCache;

    public CachedIdReader(Reader<V> inner, long count) {
      this.inner = inner;
      this.forwardCache = Caffeine.newBuilder().maximumSize(count).build(inner.forwardReader);
      this.reverseCache = Caffeine.newBuilder().maximumSize(count).build(inner.reverseReader);
    }

    @Nonnull
    @Override
    public Iterable<Pair<Integer, V>> getForward(@Nonnull List<Integer> bulk) throws IOException {
      return IterableFns.map(getForwardMap(bulk).entrySet(), Pair::new);
    }

    @Nonnull
    @Override
    public Map<Integer,V> getForwardMap(@Nonnull List<Integer> bulk) {
      return forwardCache.getAll(bulk);
    }

    @Override
    public V getForward(int id) throws IOException {
      return forwardCache.get(id);
    }

    @Nonnull
    @Override
    public Iterable<Pair<V, Integer>> getReverse(@Nonnull List<V> bulk) throws IOException {
      return IterableFns.map(getReverseMap(bulk).entrySet(), Pair::new);
    }

    @Nonnull
    @Override
    public Map<V,Integer> getReverseMap(List<V> bulk) {
      return reverseCache.getAll(bulk);
    }

    @Override
    public Integer getReverse(V item) throws IOException {
      return reverseCache.get(item);
    }

    @Nonnull
    @Override
    public Iterable<Pair<Integer, V>> items() throws IOException {
      return inner.items();
    }

    @Nonnull
    @Override
    public Iterable<Integer> ids() throws IOException {
      return inner.ids();
    }

    @Nonnull
    @Override
    public Iterable<V> values() throws IOException {
      return inner.values();
    }

    @Override
    public IdReader<V> getCached(long count) {
      return this;
    }

    @Override
    public long size() {
      return inner.size();
    }

    @Override
    public void close() throws IOException {
      inner.close();
    }
  }

}
