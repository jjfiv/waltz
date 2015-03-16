package edu.umass.cs.ciir.waltz;

import ciir.jfoley.chai.io.LinesIterable;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.lemurproject.galago.core.btree.simple.DiskMapBuilder;
import org.lemurproject.galago.core.btree.simple.DiskMapReader;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.compression.VByte;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author jfoley
 */
public class CompressDocuments {

  public static byte[] termsAsVbytes(TIntArrayList terms) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);

    for (int i = 0; i < terms.size(); i++) {
      VByte.compressInt(dos, terms.get(i));
    }

    return baos.toByteArray();
  }

  public static byte[] termsAsInts(TIntArrayList terms) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);

    for (int i = 0; i < terms.size(); i++) {
      dos.writeInt(terms.get(i));
    }

    return baos.toByteArray();
  }

  public static void main(String[] args) throws IOException {
    Vocabulary vocab = new Vocabulary(new DiskMapReader("vocab.mapping.btree"));


    File inputDir = new File("/home/jfoley/code/synthesis/robust04simple");
    File[] files = inputDir.listFiles();
    if(files == null) throw new RuntimeException("can't list files!");

    TObjectIntHashMap<String> docIdCache = new TObjectIntHashMap<>();
    try (
        DiskMapBuilder dmbvbyte = new DiskMapBuilder("corpus.vbyte");
        DiskMapBuilder dmbint4 = new DiskMapBuilder("corpus.int4");
    ) {
      int iter = 0;
      for (File file : files) {
        for (String line : LinesIterable.fromFile(file)) {
          if(++iter % 1000 == 0) {
            System.err.println("Docs: "+iter+ " cache: "+docIdCache.size());
          }
          Parameters doc = Parameters.parseString(line);
          String text = doc.getString("text");

          TIntArrayList docAsTerms = new TIntArrayList();
          for (String term : text.split(" ")) {
            int idterm = -1;
            if(docIdCache.containsKey(term)) {
              idterm = docIdCache.get(term);
            } else {
              idterm = vocab.getTermId(term);
              if(idterm < 50000) {
                docIdCache.put(term, idterm);
              }
            }
            docAsTerms.add(idterm);
          }

          byte[] docId = doc.getString("id").getBytes(StandardCharsets.UTF_8);

          byte[] vbyte = termsAsVbytes(docAsTerms);
          dmbvbyte.put(docId, vbyte);
          dmbint4.put(docId, termsAsInts(docAsTerms));

          //System.out.printf("%d\t%d\t%d\t%d\n", docAsTerms.size(), text.length(), docAsTerms.size() * 4, vbyte.length);
        }
      }
    }
  }
}
