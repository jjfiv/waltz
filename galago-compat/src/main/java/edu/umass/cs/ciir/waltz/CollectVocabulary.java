package edu.umass.cs.ciir.waltz;

import ciir.jfoley.chai.io.IO;
import ciir.jfoley.chai.io.LinesIterable;
import org.lemurproject.galago.utility.Parameters;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author jfoley
 */
public class CollectVocabulary {

  public static void reduce(List<TermStat> input) {
    List<TermStat> newOutput = new ArrayList<>(input.size() / 2);
    TermStat prev = input.get(0);
    for (int i = 1; i < input.size(); i++) {
      TermStat current = input.get(i);
      if(prev.term.equals(current.term)) {
        prev.df+=current.df;
        prev.tf+=current.tf;
      } else {
        newOutput.add(prev);
        prev = current;
      }
    }
    newOutput.add(prev);

    input.clear();
    input.addAll(newOutput);
  }

  public static void main(String[] args) throws IOException {
    File inputDir = new File("/home/jfoley/code/synthesis/robust04simple");
    File[] files = inputDir.listFiles();
    if(files == null) throw new RuntimeException("can't list files!");

    List<TermStat> vocabInMemory = new ArrayList<>();
    int iter = 0;
    for (File file : files) {
      System.err.println("New File: "+file+" size: "+vocabInMemory.size());

      List<TermStat> localVocabInMemory = new ArrayList<>();
      for (String line : LinesIterable.fromFile(file)) {
        Map<String, TermStat> termsForDoc = new HashMap<>();
        Parameters doc = Parameters.parseString(line);
        String text = doc.getString("text");
        for (String term : text.split(" ")) {
          TermStat tstat = termsForDoc.get(term);
          if(tstat == null) {
            termsForDoc.put(term, new TermStat(term, 1, 1));
          } else {
            tstat.tf++;
          }
        }

        localVocabInMemory.addAll(termsForDoc.values());
        if(localVocabInMemory.size() > 500000) {
          Collections.sort(localVocabInMemory);
          { System.err.println("Before Reduce: "+localVocabInMemory.size()); }
          reduce(localVocabInMemory);
          { System.err.println("After Reduce: "+localVocabInMemory.size()); }
          vocabInMemory.addAll(localVocabInMemory);
          localVocabInMemory.clear();
        }
      }

      vocabInMemory.addAll(localVocabInMemory);
      localVocabInMemory.clear();
      Collections.sort(vocabInMemory);
      reduce(vocabInMemory);
    }

    Collections.sort(vocabInMemory);
    reduce(vocabInMemory);
    System.err.println("Vocab In Memory Size: "+vocabInMemory.size());

    try (PrintWriter vocab = IO.openPrintWriter("vocab.gz")) {
      for (TermStat termStat : vocabInMemory) {
        vocab.println(termStat.toTSV());
      }
    }
  }
}
