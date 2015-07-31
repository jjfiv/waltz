package edu.umass.cs.ciir.waltz.coders.map.impl;

import ciir.jfoley.chai.IntMath;
import ciir.jfoley.chai.io.Directory;
import edu.umass.cs.ciir.waltz.coders.Coder;
import edu.umass.cs.ciir.waltz.coders.files.DataSource;
import edu.umass.cs.ciir.waltz.coders.files.DataSourceSkipInputStream;
import edu.umass.cs.ciir.waltz.coders.kinds.ASCII;
import edu.umass.cs.ciir.waltz.coders.kinds.FixedSize;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

/**
 * @author jfoley
 */
public class WaltzDiskMap {
  public static final int MagicLength = 128;
  public static final Coder<String> MagicCoder = new ASCII.FixedLength(MagicLength);

  public static <K> WaltzDiskMapVocabReader<K> createVocabReader(DataSource keys, long size, Coder<K> keyCoder, Comparator<K> cmp) throws IOException {
    DataSourceSkipInputStream stream = keys.stream();
    String magic = MagicCoder.read(stream);
    long count;

    switch (magic) {
      case "waltz.keys.naive":
        count = FixedSize.longs.read(stream);
        break;
      default:
        throw new IOException("Unsupported keys format: "+magic);
    }

    // TODO dispatch better on type/count
    if(count < Integer.MAX_VALUE) {
      return new NaiveVocabReader<>(IntMath.fromLong(count), size, keyCoder, cmp, stream.sourceAtCurrentPosition());
    } else {
      throw new UnsupportedOperationException();
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
