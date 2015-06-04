package edu.umass.cs.ciir.waltz.io.postings;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.postings.extents.Extent;
import edu.umass.cs.ciir.waltz.postings.extents.ExtentsList;
import edu.umass.cs.ciir.waltz.postings.extents.InterleavedExtents;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class ExtentListCoder extends Coder<ExtentsList> {
  public final Coder<Integer> sizeCoder;
  public final Coder<Integer> intCoder;

  public ExtentListCoder() {
    this(VarUInt.instance, VarUInt.instance);
  }
  public ExtentListCoder(Coder<Integer> sizeCoder, Coder<Integer> intCoder) {
    this.sizeCoder = sizeCoder;
    this.intCoder = intCoder;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Override
  public DataChunk writeImpl(ExtentsList obj) throws IOException {
    BufferList output = new BufferList();
    output.add(sizeCoder, obj.size());
    int lastExtentStart = 0;
    for (Extent extent : obj) {
      int delta = extent.begin - lastExtentStart;
      lastExtentStart = extent.begin;
      int endDelta = extent.end - extent.begin;
      output.add(intCoder, delta);
      output.add(intCoder, endDelta);
    }
    return output.compact();
  }

  @Override
  public ExtentsList readImpl(InputStream inputStream) throws IOException {
    int size = sizeCoder.readImpl(inputStream);
    InterleavedExtents results = new InterleavedExtents();
    int lastExtentStart = 0;
    for (int i = 0; i < size; i++) {
      int begin = lastExtentStart + intCoder.readImpl(inputStream);
      int end = begin + intCoder.readImpl(inputStream);
      lastExtentStart = begin;
      results.push(begin, end);
    }
    return results;
  }
}
