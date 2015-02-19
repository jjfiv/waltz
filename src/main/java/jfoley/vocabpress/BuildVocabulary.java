package jfoley.vocabpress;

import ciir.jfoley.chai.collections.util.ArrayFns;
import ciir.jfoley.chai.collections.chained.ChaiIterable;
import ciir.jfoley.chai.collections.util.ListFns;
import ciir.jfoley.chai.io.LinesIterable;
import org.lemurproject.galago.core.btree.simple.DiskMapBuilder;
import org.lemurproject.galago.core.btree.simple.DiskMapReader;
import org.lemurproject.galago.utility.ByteUtil;
import org.lemurproject.galago.utility.CmpUtil;
import org.lemurproject.galago.utility.compression.VByte;

import java.io.*;
import java.util.List;

/**
 * @author jfoley
 */
public class BuildVocabulary {

  public static void main(String[] args) throws IOException {

    List<TermStat> vocabByDF = ChaiIterable.create(LinesIterable.fromFile("vocab.gz"))
        .map(TermStat::ofTSV)
        .sorted((lhs, rhs) -> {
          int cmp = -CmpUtil.compare(lhs.df, rhs.df);
          if (cmp == 0) return -CmpUtil.compare(lhs.tf, rhs.tf);
          return cmp;
        }).intoList();

    System.out.println(ListFns.take(vocabByDF, 20));

    try (
        DiskMapBuilder vocab = new DiskMapBuilder("vocab.mapping.btree");
    ) {
      for (int i = 0; i < vocabByDF.size(); i++) {
        byte[] raw_str = ByteUtil.fromString(vocabByDF.get(i).term);
        byte[] raw_id = VByte.compressInt(i);

        // keyed-ids
        byte[] id = ArrayFns.concat(raw_id, Vocabulary.IdSuffix);
        byte[] str = ArrayFns.concat(raw_str, Vocabulary.TermSuffix);
        byte[] statsKey = ArrayFns.concat(raw_id, Vocabulary.StatsSuffix);

        vocab.put(id, raw_str);
        vocab.put(str, raw_id);
        vocab.put(statsKey, Statistics.getCodec().toBytes(vocabByDF.get(i).stats()));
      }
    }

    Vocabulary vocab = new Vocabulary(new DiskMapReader("vocab.mapping.btree"));

    for (int i = 0; i < 30; i++) {
      System.out.println(vocab.lookupTermString(i)+" "+vocab.lookupTermStats(i));
    }

  }
}
