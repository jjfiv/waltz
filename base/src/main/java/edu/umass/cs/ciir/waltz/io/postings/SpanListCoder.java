package edu.umass.cs.ciir.waltz.io.postings;

import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.postings.extents.Span;
import edu.umass.cs.ciir.waltz.postings.extents.SpanList;
import edu.umass.cs.ciir.waltz.postings.extents.InterleavedSpans;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jfoley
 */
public class SpanListCoder extends Coder<SpanList> {
  public final Coder<Integer> sizeCoder;
  public final Coder<Integer> intCoder;

  public SpanListCoder() {
    this(VarUInt.instance, VarUInt.instance);
  }
  public SpanListCoder(Coder<Integer> sizeCoder, Coder<Integer> intCoder) {
    this.sizeCoder = sizeCoder;
    this.intCoder = intCoder;
  }

  @Override
  public boolean knowsOwnSize() {
    return true;
  }

  @Nonnull
  @Override
  public DataChunk writeImpl(SpanList obj) throws IOException {
    BufferList output = new BufferList();
    output.add(sizeCoder, obj.size());
    int lastExtentStart = 0;
    for (Span extent : obj) {
      int delta = extent.begin - lastExtentStart;
      lastExtentStart = extent.begin;
      int endDelta = extent.end - extent.begin;
      output.add(intCoder, delta);
      output.add(intCoder, endDelta);
    }
    return output.compact();
  }

  @Nonnull
  @Override
  public SpanList readImpl(InputStream inputStream) throws IOException {
    int size = sizeCoder.readImpl(inputStream);
    InterleavedSpans results = new InterleavedSpans();
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
