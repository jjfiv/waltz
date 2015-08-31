package edu.umass.cs.ciir.waltz.sys.tmp;

import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.sys.KeyMetadata;
import edu.umass.cs.ciir.waltz.sys.PostingsConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author jfoley
 */
public final class TemporaryPosting<V> {
  public final PostingsConfig<?, V> cfg;
  private KeyMetadata<V> metadata;
  public ByteArrayOutputStream data;
  private int previousDocument;

  public TemporaryPosting(PostingsConfig<?, V> cfg) {
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

  public TemporaryPosting<V> write(OutputStream out) throws IOException {
    cfg.metadata.encode().write(out);
    data.writeTo(out);
    return this;
  }

  public void close() throws IOException {
    this.data = null;
  }
}
