package edu.umass.cs.ciir.waltz.index.mem;

/**
 * @author jfoley.
 */
public interface InternSpace<Val> {
	public int getId(Val query);
	public Val getValue(int query);
	public void put(int first, Val second);
  public int insertOrGet(Val k);
}
