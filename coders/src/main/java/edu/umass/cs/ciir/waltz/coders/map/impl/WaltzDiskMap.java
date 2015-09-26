package edu.umass.cs.ciir.waltz.coders.map.impl;

import ciir.jfoley.chai.IntMath;
import ciir.jfoley.chai.io.Directory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.files.DataSource;
import edu.umass.cs.ciir.waltz.coders.files.DataSourceSkipInputStream;
import edu.umass.cs.ciir.waltz.coders.kinds.ASCII;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;
import edu.umass.cs.ciir.waltz.coders.map.impl.vocab.NaiveVocabReader;
import edu.umass.cs.ciir.waltz.coders.map.impl.vocab.SamplingNaiveVocabReader;
import edu.umass.cs.ciir.waltz.coders.map.impl.vocab.VocabConfig;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

/**
 * @author jfoley
 */
public class WaltzDiskMap {
  public static final int MagicLength = 128;
  public static final Coder<String> MagicCoder = new ASCII.FixedLength(MagicLength);

  public static <K> WaltzDiskMapVocabReader<K> createVocabReader(DataSource keys, Coder<K> keyCoder, Comparator<K> cmp) throws IOException {
    VocabConfig<K> cfg = new VocabConfig<>(keyCoder, cmp);
    DataSourceSkipInputStream stream = keys.stream();
    String magic = MagicCoder.read(stream);
    long count;

    switch (magic) {
      case "waltz.keys.NaiveVocabWriter":
        count = FixedSize.longs.read(stream);
        break;
      case "waltz.keys.naive": // old, buggy format
      default:
        throw new IOException("Unsupported keys format: "+magic);
    }

    System.out.println("NaiveVocabReader.count="+count);

    // TODO dispatch better on type/count
    if(count < 1_000_000 && count >= 0) {
      return new NaiveVocabReader<>(IntMath.fromLong(count), cfg, stream.sourceAtCurrentPosition());
    } else {
      return new SamplingNaiveVocabReader<>(count, cfg, stream.sourceAtCurrentPosition());
    }
  }

  public static String getKeyFileName(Directory baseDir, String baseName) {
    return baseDir.childPath(baseName+".keys");
  }
  public static String getValueFileName(Directory baseDir, String baseName) {
    return baseDir.childPath(baseName+".values");
  }
  public static File getKeySortDirectory(Directory baseDir, String baseName) {
    return baseDir.child(baseName+".ksort");
  }

}
