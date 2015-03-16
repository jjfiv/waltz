package edu.umass.cs.ciir.waltz;

import ciir.jfoley.chai.io.IO;
import ciir.jfoley.chai.string.StrUtil;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.TagTokenizer;
import org.lemurproject.galago.core.parse.TrecTextParser;
import org.lemurproject.galago.core.util.DocumentSplitFactory;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.tools.Arguments;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * @author jfoley
 */
public class ConvertToSimple {
  public static void convert(String input, String output, Parameters argp) throws IOException {
    TagTokenizer tok = new TagTokenizer();
    try (
        TrecTextParser parser = new TrecTextParser(DocumentSplitFactory.file(input), argp);
        PrintWriter out = IO.openPrintWriter(output)
    ) {
      while(true) {
        Document doc = parser.nextDocument();
        if(doc == null) break;
        System.err.println("# "+doc.name);
        tok.tokenize(doc);
        out.println(Parameters.parseArray("text", Utility.join(doc.terms, " "), "id", doc.name));
      }
    }

    System.err.println("Done!");
  }

  public static void main(String[] args) throws IOException {
    Parameters argp = Arguments.parse(args);

    String basePath = "/mnt/scratch/jfoley/robust04raw/";
    List<String> pieces = Arrays.asList(
        "ft91.dat.gz", "ft92.dat.gz", "ft93.dat.gz", "ft94.dat.gz",
        "fr94.dat.gz", "fbis5.dat.gz", "latimes.dat.gz");

    for (String piece : pieces) {
      String input = basePath + piece;
      String output = StrUtil.removeBack(piece, ".dat.gz") + ".json.gz";
      if(new File(output).exists()) {
        continue;
      }
      convert(input, output, argp);
      System.err.println("Done: " + piece);
    }

    System.err.println("Done!");
  }
}
