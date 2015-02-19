package jfoley.vocabpress;

import ciir.jfoley.chai.collections.util.ArrayFns;
import ciir.jfoley.chai.collections.chained.ChaiIterable;
import ciir.jfoley.chai.collections.util.ListFns;
import ciir.jfoley.chai.io.LinesIterable;
import org.lemurproject.galago.core.btree.simple.DiskMapBuilder;
import org.lemurproject.galago.utility.ByteUtil;
import org.lemurproject.galago.utility.CmpUtil;
import org.lemurproject.galago.utility.compression.VByte;

import java.io.*;
import java.util.List;

/**
 * @author jfoley
 */
public class LoadVocabulary {

  public static void main(String[] args) throws IOException {

    List<TermStat> vocabByDF = ChaiIterable.create(LinesIterable.fromFile("vocab.gz"))
        .map(TermStat::ofTSV)
        .sorted((lhs, rhs) -> {
          int cmp = -CmpUtil.compare(lhs.df, rhs.df);
          if (cmp == 0) return -CmpUtil.compare(lhs.tf, rhs.tf);
          return cmp;
        }).intoList();

    System.out.println(ListFns.take(vocabByDF, 20));

    String idSuffix = "/i";
    String termSuffix = "/t";
    String statsSuffix = "/s";
    try (
        DiskMapBuilder vocab = new DiskMapBuilder("vocab.mapping.btree");
    ) {
      for (int i = 0; i < vocabByDF.size(); i++) {
        byte[] id = ArrayFns.concat(VByte.compressInt(i), ByteUtil.fromString(idSuffix));
        byte[] str = ByteUtil.fromString(vocabByDF.get(i).term+termSuffix);
        byte[] statsKey = ArrayFns.concat(VByte.compressInt(i), ByteUtil.fromString(statsSuffix));

        vocab.put(id, str);
        vocab.put(str, id);
        vocab.put(statsKey, Statistics.getCodec().toBytes(vocabByDF.get(i).stats()));
      }
    }

    try (DiskMapBuilder dmb = new DiskMapBuilder("vocab.btree")) {
      for (String line : LinesIterable.fromFile("vocab.gz")) {
        TermStat stat = TermStat.ofTSV(line);
        dmb.put(ByteUtil.fromString(stat.term), Statistics.getCodec().toBytes(stat.stats()));
      }
    }
  }
}
