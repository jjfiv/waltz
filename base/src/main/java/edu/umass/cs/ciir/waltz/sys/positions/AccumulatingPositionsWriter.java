package edu.umass.cs.ciir.waltz.sys.positions;

import ciir.jfoley.chai.collections.list.IntList;
import ciir.jfoley.chai.collections.util.MapFns;
import ciir.jfoley.chai.fn.GenerateFn;
import edu.umass.cs.ciir.waltz.io.postings.ArrayPosList;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Because generating the actual positions-lists is the same every time.
 * @author jfoley
 */
public class AccumulatingPositionsWriter<K> implements Closeable {
  int lastDoc = -1;
  HashMap<K, IntList> docHits = new HashMap<>();

  public final PIndexWriter<K, PositionsList> output;

  public AccumulatingPositionsWriter(PIndexWriter<K, PositionsList> output) {
    this.output = output;
  }

  public void add(K word, int doc, int pos) {
    if (lastDoc != doc) {
      flush();
    }
    lastDoc = doc;
    MapFns.extendCollectionInMap(docHits, word, pos, (GenerateFn<IntList>) IntList::new);
  }

  private void flush() {
    if (lastDoc != -1) {
      for (Map.Entry<K, IntList> kv : docHits.entrySet()) {
        output.add(kv.getKey(), lastDoc, new ArrayPosList(kv.getValue()));
      }
      docHits.clear();
    }
  }

  @Override
  public void close() throws IOException {
    flush();
    output.close();
  }
}
