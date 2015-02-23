package jfoley.vocabpress;

import ciir.jfoley.chai.io.LinesIterable;
import gnu.trove.list.array.TIntArrayList;
import org.lemurproject.galago.core.btree.simple.DiskMapReader;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.compression.VByte;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

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

  public static void main(String[] args) throws IOException {
    Vocabulary vocab = new Vocabulary(new DiskMapReader("vocab.mapping.btree"));


    File inputDir = new File("/home/jfoley/code/synthesis/robust04simple");
    File[] files = inputDir.listFiles();
    if(files == null) throw new RuntimeException("can't list files!");

    for (File file : files) {
      int iter = 0;
      for (String line : LinesIterable.fromFile(file)) {
        if(++iter > 30) break;
        Parameters doc = Parameters.parseString(line);
        String text = doc.getString("text");
        TIntArrayList docAsTerms = new TIntArrayList();
        for (String term : text.split(" ")) {
          docAsTerms.add(vocab.getTermId(term));
        }

        byte[] vbyte = termsAsVbytes(docAsTerms);

        System.out.printf("%d\t%d\t%d\t%d\n", docAsTerms.size(), text.length(), docAsTerms.size()*4, vbyte.length);
      }
    }
  }
}
