package edu.umass.cs.ciir.waltz.coders.map.zip;

import ciir.jfoley.chai.collections.Pair;
import ciir.jfoley.chai.io.archive.ZipArchive;
import ciir.jfoley.chai.io.archive.ZipArchiveEntry;
import ciir.jfoley.chai.io.archive.ZipWriter;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.kinds.CharsetCoders;
import edu.umass.cs.ciir.waltz.coders.map.IOMap;
import edu.umass.cs.ciir.waltz.coders.map.IOMapWriter;
import edu.umass.cs.ciir.waltz.coders.streams.SkipInputStream;
import edu.umass.cs.ciir.waltz.coders.streams.StaticStream;

import java.io.IOException;
import java.util.*;

/**
 * A Zip file can be an IOMap of String to any Value.
 * @author jfoley.
 */
public class ZipIOMap<V> implements IOMap<String, V> {
  private final ZipArchive archive;
  private final Coder<V> valCoder;
  Map<String, ZipArchiveEntry> entries;

  public ZipIOMap(ZipArchive archive, Coder<V> valCoder) {
    this.valCoder = valCoder;
    this.archive = archive;
    entries = new HashMap<>();
    for (ZipArchiveEntry entry : archive.listEntries()) {
      entries.put(entry.getName(), entry);
    }
  }

  @Override
  public long keyCount() {
    return archive.listEntries().size();
  }

  @Override
  public Map<String, Object> getConfig() {
    return Collections.emptyMap();
  }

  @Override
  public V get(String key) throws IOException {
    ZipArchiveEntry entry = archive.getByName(key);
    if(entry == null) return null;
    return valCoder.read(entry.getInputStream());
  }

  @Override
  public StaticStream getSource(String key) throws IOException {
    ZipArchiveEntry entry = archive.getByName(key);
    if(entry == null) return null;
    return new StaticStream() {
      @Override
      public SkipInputStream getNewStream() {
        try {
          return SkipInputStream.wrap(entry.getInputStream());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public long length() {
        return entry.rawZipEntry().getSize();
      }
    };
  }

  @Override
  public List<Pair<String, V>> getInBulk(List<String> keys) throws IOException {
    List<Pair<String, V>> out = new ArrayList<>();
    for (String key : keys) {
      V vals = get(key);
      if(vals != null) {
        out.add(Pair.of(key, vals));
      }
    }
    return out;
  }

  @Override
  public void close() throws IOException {
    archive.close();
    entries.clear();
  }

  public static class Writer<V> implements IOMapWriter<String, V> {
    private final ZipWriter writer;
    private final Coder<V> valCoder;

    public Writer(ZipWriter wr, Coder<V> valCoder) {
      this.writer = wr;
      this.valCoder = valCoder;
    }

    @Override
    public void put(String key, V val) throws IOException {
      writer.write(key, input -> {
        try {
          valCoder.write(input, val);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }

    @Override
    public IOMapWriter<String, V> getSorting() throws IOException {
      return this;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public Coder<String> getKeyCoder() {
      return CharsetCoders.utf8LengthPrefixed;
    }

    @Override
    public Coder<V> getValueCoder() {
      return valCoder;
    }

    @Override
    public void flush() throws IOException {
      writer.flush();
    }
  }
}
