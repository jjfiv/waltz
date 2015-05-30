package edu.umass.cs.ciir.waltz.coders.map;

import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.sorter.ExternalSortingWriter;
import edu.umass.cs.ciir.waltz.coders.tuple.DiskMapAtom;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * @author jfoley.
 */
public class SortingIOMapWriter<K extends Comparable<K>,V> implements IOMapWriter<K,V>, Closeable ,Flushable {
  private final TemporaryDirectory tmpdir;
  private final ExternalSortingWriter<DiskMapAtom<K, V>> sorter;
  private final IOMapWriter<K, V> inner;

  public SortingIOMapWriter(IOMapWriter<K,V> inner) throws IOException {
    this.tmpdir = new TemporaryDirectory();
    this.sorter = new ExternalSortingWriter<>(tmpdir.get(), DiskMapAtom.getCoder(inner.getKeyCoder(), inner.getValueCoder()));
    this.inner = inner;
  }

  public void add(K key, V value) {
    sorter.process(new DiskMapAtom<>(key, value));
  }

  @Override
  public void put(K key, V val) {
    add(key, val);
  }

  @Override
  public IOMapWriter<K, V> getSorting() throws IOException {
    return this;
  }

  @Override
  public void close() throws IOException {
    sorter.close();

    // write sorted elements to inner map writer
    for (DiskMapAtom<K, V> atom : sorter.getOutput()) {
      inner.put(atom.left, atom.right);
    }

    inner.close(); // flush
    tmpdir.close(); // delete temporary (sorted) data
  }

  @Override
  public Coder<K> getKeyCoder() {
    return inner.getKeyCoder();
  }

  @Override
  public Coder<V> getValueCoder() {
    return inner.getValueCoder();
  }

  @Override
  public void flush() throws IOException {
    sorter.flush();
  }
}
