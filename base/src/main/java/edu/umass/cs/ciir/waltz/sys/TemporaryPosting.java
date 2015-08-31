package edu.umass.cs.ciir.waltz.sys;

import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author jfoley
 */
public final class TemporaryPosting<M extends KeyMetadata<V, M>, V> {
  public final PostingsConfig<?, M, V> cfg;
  private M metadata;
  public ByteArrayOutputStream data;
  private int previousDocument;

  public TemporaryPosting(PostingsConfig<?, M, V> cfg) {
    this.cfg = cfg;
    this.data = new ByteArrayOutputStream();
    this.previousDocument = 0;
    this.metadata = cfg.newMetadata();
  }

  public void add(int document, V payload) {
    assert (previousDocument == 0 || document > previousDocument);
    metadata.accumulate(document, payload);
    VarUInt.instance.writePrim(data, document - previousDocument);
    cfg.valCoder.write(data, payload);
    previousDocument = document;
  }

  public TemporaryPosting<M, V> write(OutputStream out) throws IOException {
    cfg.metadataCoder.write(out, metadata);
    data.writeTo(out);
    return this;
  }

  public void close() throws IOException {
    this.data = null;
  }
}
