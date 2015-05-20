package edu.umass.cs.ciir.waltz.io.postings;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.io.coders.DeltaIntListCoder;
import edu.umass.cs.ciir.waltz.postings.positions.PositionsList;
import edu.umass.cs.ciir.waltz.postings.positions.SimplePositionsList;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author jfoley
 */
public class PositionsListCoder extends Coder<PositionsList> {
  private final Coder<List<Integer>> innerCoder;

  public PositionsListCoder() {
    this(new DeltaIntListCoder());
  }
  public PositionsListCoder(Coder<List<Integer>> innerCoder) {
    assert(innerCoder.knowsOwnSize());
    this.innerCoder = innerCoder;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Override
  public DataChunk writeImpl(PositionsList obj) throws IOException {
    return innerCoder.writeImpl(obj.toList());
  }

  @Override
  public PositionsList readImpl(InputStream inputStream) throws IOException {
    return new SimplePositionsList(innerCoder.readImpl(inputStream));
  }
}
