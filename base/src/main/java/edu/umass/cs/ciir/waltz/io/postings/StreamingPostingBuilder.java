package edu.umass.cs.ciir.waltz.io.postings;

import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.map.RawIOMapWriter;
import edu.umass.cs.ciir.waltz.coders.sorter.ExternalSortingWriter;
import edu.umass.cs.ciir.waltz.coders.tuple.MapPostingAtom;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Objects;

/**
 * Stores a V per K,document pair.. First sorts using a temporary directory, then collects data into a Map on close().
 * @author jfoley
 */
public class StreamingPostingBuilder<K extends Comparable<K>,V> implements Closeable, Flushable {
  public final Coder<K> keyCoder;
  public final Coder<V> valCoder;
  private final RawIOMapWriter rawMapWriter;
  private final TemporaryDirectory sortingDir;
  private final ExternalSortingWriter<MapPostingAtom<K, V>> sortingWriter;

  public StreamingPostingBuilder(Coder<K> keyCoder, Coder<V> valCoder, RawIOMapWriter mapWriter) throws IOException {
    this.keyCoder = keyCoder;
    this.valCoder = valCoder;
    this.sortingDir = new TemporaryDirectory();
    this.sortingWriter = new ExternalSortingWriter<>(
        sortingDir.get(),
        new MapPostingAtom.MPACoder<>(keyCoder, valCoder)
    );
    this.rawMapWriter = mapWriter;
    //this.rawMapWriter = new IOMapWriter<>(mapWriter, keyCoder, new SimplePostingListFormat.PostingCoder<>(valCoder));
  }

  public void add(K term, int document, V value) {
    add(new MapPostingAtom<>(term, document, value));
  }

  private void add(MapPostingAtom<K, V> atom) {
    sortingWriter.process(atom);
  }

  public ValueBuilder<V> makeValueBuilder() throws IOException {
    return new SimplePostingListFormat.PostingValueBuilder<>(valCoder);
  }

  @Override
  public void close() throws IOException {
    // finish sorting:
    sortingWriter.close();

    // read sortingWriter into mapWriter
    try (RawIOMapWriter writer = this.rawMapWriter) {
      K lastKey = null;
      ValueBuilder<V> valBuilder = makeValueBuilder();
      for (MapPostingAtom<K, V> atom : sortingWriter.getOutput()) {
        if (lastKey == null) {
          lastKey = atom.getTerm();
        }
        if (!Objects.equals(lastKey, atom.getTerm())) {
          // flush old output, make new builder
          writer.put(keyCoder.writeData(atom.getTerm()), valBuilder.getOutput());
          valBuilder = makeValueBuilder();

          // update last key, continue;
          lastKey = atom.getTerm();
        }
        valBuilder.add(atom.getDocument(), atom.getValue());
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
