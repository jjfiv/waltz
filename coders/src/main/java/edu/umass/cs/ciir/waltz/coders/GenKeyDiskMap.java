package edu.umass.cs.ciir.waltz.coders;

import ciir.jfoley.chai.IntMath;
import edu.umass.cs.ciir.waltz.coders.files.DataSource;
import edu.umass.cs.ciir.waltz.coders.files.FileChannelSource;
import edu.umass.cs.ciir.waltz.coders.files.FileSink;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriter;
import edu.umass.cs.ciir.waltz.coders.map.SortingIOMapWriter;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

/**
 * This is a disk map that stores arbitrary values at long-valued keys.
 * It stores data for each object, and an object-offset-table, the latter of which is trivially O(1) indexed.
 *
 * It consists of two files:
 *  - an offset file, ".offset"
 *  - a values file: ".values"
 * @author jfoley.
 */
public class GenKeyDiskMap {
  public static class Writer<V> implements IOMapWriter<Long, V>, Closeable {
    public final FileSink offsetFile;
    public final FileSink valuesFile;
    public static final Coder<Long> offsetCoder = FixedSize.longs;
    public long nextIdentifier = 0;
    private final Coder<V> valCoder;

    public Writer(FileSink offsetFile, FileSink valuesFile, Coder<V> valCoder) {
      this.offsetFile = offsetFile;
      this.valuesFile = valuesFile;
      this.valCoder = valCoder;
    }
    public static <V> Writer<V> createNew(String basePath, Coder<V> valCoder) throws IOException {
      return new Writer<>(
          new FileSink(basePath+".offset"),
          new FileSink(basePath+".values"), valCoder);
    }

    long nextOffset() throws IOException {
      return valuesFile.tell();
    }

    /**
     * Writes the next value from the given ByteBuffer.
     * @param buf the data to write.
     * @return the identifier assigned to that value.
     * @throws IOException
     */
    public long writeNextValue(ByteBuffer buf) throws IOException {
      long start = nextOffset();
      valuesFile.write(buf);
      offsetFile.write(offsetCoder, start);
      return nextIdentifier++;
    }

    /**
     * Writes the next value.
     * @param value the value itself.
     * @return the identifier (int/long) key assigned to this value.
     * @throws IOException
     */
    public long writeNextValue(V value) throws IOException {
      long start = nextOffset();
      valuesFile.write(valCoder, value);
      offsetFile.write(offsetCoder, start);
      return nextIdentifier++;
    }

    @Override
    public void put(Long key, V val) throws IOException {
      long currentId = nextIdentifier++;
      assert(currentId == key) : "GenKeyDiskMap only supports pre-sorted, ";

    }

    @Override
    public IOMapWriter<Long, V> getSorting() throws IOException {
      return new SortingIOMapWriter<>(this);
    }

    @Override
    public void close() throws IOException {
      offsetFile.close();
      valuesFile.close();
    }

    /**
     * Used by sorting wrapper, needs only be able to encode keys, not be exact implementation.
     * @return a fixed size key coder for now
     */
    @Override
    public Coder<Long> getKeyCoder() {
      return FixedSize.longs;
    }

    @Override
    public Coder<V> getValueCoder() {
      return valCoder;
    }

    @Override
    public void flush() throws IOException {
      offsetFile.flush();
      valuesFile.flush();
    }
  }

  public static class Reader implements Closeable {
    private final DataSource offsetFile;
    private final DataSource valuesFile;
    private final long count;

    public Reader(DataSource offsetFile, DataSource valuesFile) throws IOException {
      this.offsetFile = offsetFile;
      this.valuesFile = valuesFile;
      this.count = offsetFile.size() / 8;
    }

    public long count() {
      return this.count;
    }

    public static Reader openFiles(String basePath) throws IOException {
      return new Reader(
          new FileChannelSource(basePath+".offset"),
          new FileChannelSource(basePath+".values"));
    }

    @Override
    public void close() throws IOException {
      offsetFile.close();
      valuesFile.close();
    }

    public <T> T getValue(Coder<T> coder, long index) throws IOException {
      if(index >= count) throw new NoSuchElementException("Can't get item at index="+index+" where there are only "+count+" items!");
      long valueOffset = offsetFile.readLong(index*8);
      long nextValueOffset = (index+1 == count) ? valuesFile.size() : offsetFile.readLong((index+1)*8);
      int valueSize = IntMath.fromLong(nextValueOffset - valueOffset);
      return coder.read(valuesFile.read(valueOffset, valueSize));
    }
  }
}
