package edu.umass.cs.ciir.waltz.io.postings;

import ciir.jfoley.chai.IntMath;
import ciir.jfoley.chai.io.StreamFns;
import ciir.jfoley.chai.io.TemporaryDirectory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.data.BufferList;
import edu.umass.cs.ciir.waltz.coders.data.ByteArray;
import edu.umass.cs.ciir.waltz.coders.data.DataChunk;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.map.RawIOMapWriter;
import edu.umass.cs.ciir.waltz.coders.sorter.ExternalSortingWriter;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
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

  public static class ByteKeyPosting<V> implements Comparable<ByteKeyPosting<?>> {
    public final ByteArray key;
    public final int document;
    public final V value;
    public ByteKeyPosting(ByteArray key, int document, V value) {
      this.key = key;
      this.document = document;
      this.value = value;
    }
    @Override
    public int compareTo(@Nonnull ByteKeyPosting<?> o) {
      int cmp = key.compareTo(o.key);
      if(cmp != 0) return cmp;
      return Integer.compare(document, o.document);
    }
  }

  private static class ByteKeyPostingCoder<V> extends Coder<ByteKeyPosting<V>> {
    private final Coder<V> valCoder;

    public ByteKeyPostingCoder(Coder<V> valCoder) {
      this.valCoder = valCoder.lengthSafe();
    }
    @Override
    public boolean knowsOwnSize() {
      return true;
    }

    @Nonnull
    @Override
    public DataChunk writeImpl(ByteKeyPosting<V> obj) throws IOException {
      BufferList output = new BufferList();
      output.add(VarUInt.instance, IntMath.fromLong(obj.key.byteCount()));
      output.add(obj.key);
      output.add(VarUInt.instance, obj.document);
      output.add(valCoder, obj.value);
      return output;
    }

    @Nonnull
    @Override
    public ByteKeyPosting<V> readImpl(InputStream inputStream) throws IOException {
      int size = VarUInt.instance.readImpl(inputStream);
      ByteArray key = new ByteArray(StreamFns.readBytes(inputStream, size));
      int document = VarUInt.instance.readImpl(inputStream);
      V value = valCoder.readImpl(inputStream);
      return new ByteKeyPosting<>(key, document, value);
    }
  }

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

  public ValueBuilder<V> makeValueBuilder() throws IOException {
    return new SimplePostingListFormat.PostingValueBuilder<>(valCoder);
  }

  @Override
  public void close() throws IOException {
    // finish sorting:
    sortingWriter.close();

    // read sortingWriter into mapWriter
    try (RawIOMapWriter writer = this.rawMapWriter) {
      ByteArray lastKey = null;
      ValueBuilder<V> valBuilder = makeValueBuilder();
      for (ByteKeyPosting<V> atom : sortingWriter.getOutput()) {
        if (lastKey == null) {
          lastKey = atom.key;
        }
        if (!Objects.equals(lastKey, atom.key)) {
          // flush old output, make new builder
          writer.put(lastKey, valBuilder.getOutput());
          valBuilder.close();

          // update last key, continue;
          lastKey = atom.key;
          valBuilder = makeValueBuilder();
        }
        valBuilder.add(atom.document, atom.value);
      }
      // put the last one:
      if (lastKey != null) {
        writer.put(lastKey, valBuilder.getOutput());
        valBuilder.close();
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
