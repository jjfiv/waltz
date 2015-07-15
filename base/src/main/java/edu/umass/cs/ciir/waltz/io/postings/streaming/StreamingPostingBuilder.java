package edu.umass.cs.ciir.waltz.io.postings.streaming;

import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.ByteArray;
import edu.umass.cs.ciir.waltz.coders.map.RawIOMapWriter;
import edu.umass.cs.ciir.waltz.coders.sorter.ExternalSortingWriter;
import edu.umass.cs.ciir.waltz.io.postings.format.BlockedPostingValueBuilder;
import edu.umass.cs.ciir.waltz.io.postings.AbstractValueBuilder;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Objects;

/**
 * Stores a V per K,document pair.. First sorts using a temporary directory, then collects data into a Map on close().
 * @author jfoley
 */
public class StreamingPostingBuilder<K, V> implements Closeable, Flushable {
  public final Coder<K> keyCoder;
  public final Coder<V> valCoder;
  private final RawIOMapWriter rawMapWriter;
  private final TemporaryDirectory sortingDir;
  private final ExternalSortingWriter<ByteKeyPosting<V>> sortingWriter;

  public StreamingPostingBuilder(Coder<K> keyCoder, Coder<V> valCoder, RawIOMapWriter mapWriter) throws IOException {
    this.keyCoder = keyCoder;
    this.valCoder = valCoder;
    this.sortingDir = new TemporaryDirectory();
    this.sortingWriter = new ExternalSortingWriter<>(
        sortingDir.get(),
        new ByteKeyPostingCoder<>(valCoder)
    );
    this.rawMapWriter = mapWriter;
  }

  public void add(K term, int document, V value) {
    add(new ByteKeyPosting<>(ByteArray.of(keyCoder.writeData(term)), document, value));
  }

  private void add(ByteKeyPosting<V> atom) {
    sortingWriter.process(atom);
  }

  public AbstractValueBuilder<V> makeValueBuilder() throws IOException {
    return new BlockedPostingValueBuilder<>(valCoder);
  }

  @Override
  public void close() throws IOException {
    // finish sorting:
    sortingWriter.close();

    // read sortingWriter into mapWriter
    try (RawIOMapWriter writer = this.rawMapWriter) {
      ByteArray lastKey = null;
      AbstractValueBuilder<V> valBuilder = makeValueBuilder();
      for (ByteKeyPosting<V> atom : sortingWriter.getOutput()) {
        if (lastKey == null) {
          lastKey = atom.key;
        }
        if (!Objects.equals(lastKey, atom.key)) {
          // flush old output, make new builder
          writer.put(lastKey, valBuilder.getOutput());

          // update last key, continue;
          lastKey = atom.key;
          valBuilder = makeValueBuilder();
        }
        valBuilder.add(atom.document, atom.value);
      }
      // put the last one:
      if (lastKey != null) {
        writer.put(lastKey, valBuilder.getOutput());
      }
    }

    // delete sorted data when done.
    sortingDir.close();
  }

  @Override
  public void flush() throws IOException {
    this.sortingWriter.flush();
  }
}
