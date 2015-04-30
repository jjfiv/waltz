package edu.umass.cs.ciir.waltz.index.intern;

/**
 * @author jfoley.
 */
public interface InternSpace<Val> extends StaticInternSpace<Val> {
  void put(int first, Val second);
  int insertOrGet(Val k);
}
