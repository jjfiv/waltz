package jfoley.vocabpress;

import org.lemurproject.galago.core.btree.simple.DiskMapWrapper;
import org.lemurproject.galago.utility.Parameters;

/**
* @author jfoley
*/
public class Statistics {
  public int tf;
  public int df;

  public Statistics() { this(0,0); }

  public Statistics(int tf, int df) {
    this.tf = tf;
    this.df = df;
  }

  @Override
  public String toString() {
    return Parameters.parseArray("tf", tf, "df", df).toString();
  }

  public static DiskMapWrapper.Codec<Statistics> getCodec() {
    return StatsCodec.instance;
  }

}
