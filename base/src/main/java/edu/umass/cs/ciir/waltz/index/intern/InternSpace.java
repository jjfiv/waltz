package edu.umass.cs.ciir.waltz.index.intern;

/**
 * @author jfoley.
 */
public interface InternSpace<Val> extends StaticInternSpace<Val> {
  public void put(int first, Val second);
  public int insertOrGet(Val k);
}
