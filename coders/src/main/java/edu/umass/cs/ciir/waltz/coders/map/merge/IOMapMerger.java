package edu.umass.cs.ciir.waltz.coders.map.merge;

import ciir.jfoley.chai.collections.util.Comparing;
import ciir.jfoley.chai.fn.TransformFn;
import edu.umass.cs.ciir.waltz.coders.map.IOMap;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriter;
import edu.umass.cs.ciir.waltz.coders.reduce.Deduplicator;
import edu.umass.cs.ciir.waltz.coders.reduce.ReducingIterator;
import edu.umass.cs.ciir.waltz.coders.sorter.IterableSortedReader;
import edu.umass.cs.ciir.waltz.coders.sorter.MergingRunReader;
import edu.umass.cs.ciir.waltz.coders.sorter.SortedReader;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is a memory hog for now.
 * @author jfoley
 */
public class IOMapMerger<K, V> implements Closeable {
  public final List<IOMap<K,V>> inputs;
  public final TransformFn<List<V>, V> valueCombiner;
  public final IOMapWriter<K,V> writer;

  public IOMapMerger(IOMapWriter<K, V> writer, List<IOMap<K, V>> inputs, TransformFn<List<V>, V> valueCombiner) {
    this.inputs = inputs;
    this.valueCombiner = valueCombiner;
    this.writer = writer;
  }

  public void merge() throws IOException {
    List<SortedReader<K>> keys = new ArrayList<>();
    for (IOMap<K, V> input : inputs) {
      keys.add(new IterableSortedReader<>(input.keys(), Comparing.defaultComparator()));
    }
    MergingRunReader<K> mergedKeys = new MergingRunReader<>(keys);

    Iterator<K> dedup = new ReducingIterator<>(new Deduplicator<>(), mergedKeys);

    List<V> values = new ArrayList<>();
    while(dedup.hasNext()) {
      K key = dedup.next();
      for (IOMap<K, V> input : inputs) {
        values.add(input.get(key));
      }
      assert(values.size() >= 1);
      V outputValue;
      if(values.size() == 1) {
        outputValue = values.get(0);
      } else {
        outputValue = valueCombiner.transform(values);
      }
      writer.put(key, outputValue);
    }
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}
