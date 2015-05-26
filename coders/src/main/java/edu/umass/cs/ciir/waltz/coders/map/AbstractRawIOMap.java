package edu.umass.cs.ciir.waltz.coders.map;

import ciir.jfoley.chai.collections.Pair;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author jfoley
 */
public abstract class AbstractRawIOMap implements RawIOMap {
  @Override
  public Map<String, Object> getConfig() {
    return Collections.emptyMap();
  }

  @Override
  public List<Pair<DataChunk, StaticStream>> getInBulk(List<DataChunk> keys) throws IOException {
    List<Pair<DataChunk, StaticStream>> out = new ArrayList<>();
    for (DataChunk key : keys) {
      StaticStream vals = get(key);
      if(vals != null) {
        out.add(Pair.of(key, vals));
      }
    }
    return out;
  }
}
