package jfoley.vocabpress;

/**
* @author jfoley
*/
public class TermStat implements Comparable<TermStat> {
  public String term;
  public int tf;
  public int df;

  public TermStat(String term, int tf, int df) {
    this.term = term;
    this.tf = tf;
    this.df = df;
  }

  public String toTSV() {
    return term +"\t"+tf+"\t"+df;
  }
  public static TermStat ofTSV(String input) {
    String[] col = input.split("\t");
    assert(col.length == 3);
    return new TermStat(col[0], Integer.parseInt(col[1]), Integer.parseInt(col[2]));
  }

  @Override
  public String toString() {
    return "{"+term+", "+tf+","+df+"}";
  }

  @Override
  public int compareTo(TermStat o) {
    return this.term.compareTo(o.term);
  }

  public Statistics stats() {
    return new Statistics(tf, df);
  }
}
