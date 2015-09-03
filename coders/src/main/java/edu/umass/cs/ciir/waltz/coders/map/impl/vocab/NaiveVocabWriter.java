package edu.umass.cs.ciir.waltz.coders.map.impl.vocab;

import edu.umass.cs.ciir.waltz.coders.files.FileSink;
import edu.umass.cs.ciir.waltz.coders.files.WriteLongLater;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.kinds.VarUInt;
import edu.umass.cs.ciir.waltz.coders.map.impl.WaltzDiskMap;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author jfoley
 */
public class NaiveVocabWriter<K> extends AVocabWriter<K> {
  public final FileSink keysFile;
  private final VocabConfig<K> cfg;
  public long keyCount;
  WriteLongLater keyCountUpdater;

  public NaiveVocabWriter(FileSink keysFile, VocabConfig<K> config) {
    this.keysFile = keysFile;
    this.cfg = config;
  }

  @Override
  public void onEntry(@Nonnull K key, long start, int size) throws IOException {
    assert (this.key == null);
    keyCount++;
    keysFile.write(cfg.keyCoder, key);
    keysFile.write(FixedSize.longs, start);
    keysFile.write(VarUInt.instance, size);
  }

  @Override
  public void writeHeader() throws IOException {
    keysFile.write(WaltzDiskMap.MagicCoder, "waltz.keys.NaiveVocabWriter");
    keyCountUpdater = new WriteLongLater(keysFile);
  }

  @Override
  public void close() throws IOException {
    keyCountUpdater.write(keyCount);
    keysFile.close();
  }
}
