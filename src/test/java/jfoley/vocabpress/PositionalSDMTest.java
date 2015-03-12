package jfoley.vocabpress;

import jfoley.vocabpress.index.mem.MemoryPositionsIndex;
import org.junit.Test;
import org.lemurproject.galago.utility.Parameters;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class PositionalSDMTest {

  static List<String> tokens(String input) {
    return Arrays.asList(input.toLowerCase().split("\\s+"));
  }

  @Test
  public void testDirichlet() throws Exception {
    MemoryPositionsIndex index = new MemoryPositionsIndex();
    index.addDocument("fox", tokens("a fox is a mammal"));
    index.addDocument("quick", tokens("the quick brown fox jumped over the lazy dog"));

    PositionalSDM sdm = new PositionalSDM(index, tokens("quick brown fox"), Parameters.parseArray("mu", 1.0));
    double scoreFox = sdm.score(0);
    double scoreQuick = sdm.score(1);

    assertTrue(scoreQuick > scoreFox);
    //System.out.println(Arrays.asList(scoreFox, scoreQuick));
  }
}